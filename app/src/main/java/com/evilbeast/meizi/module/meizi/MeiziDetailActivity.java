package com.evilbeast.meizi.module.meizi;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.design.widget.AppBarLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.animation.DecelerateInterpolator;
import android.widget.Toast;

import com.evilbeast.meizi.R;
import com.evilbeast.meizi.base.RxBaseActivity;
import com.evilbeast.meizi.entity.meizi.MeiZi;
import com.evilbeast.meizi.entity.meizi.MeiZiGroup;
import com.evilbeast.meizi.network.Api.MeiZiApi;
import com.evilbeast.meizi.network.RetrofixHelper;
import com.evilbeast.meizi.rx.RxBus;
import com.evilbeast.meizi.utils.ConstantUtil;
import com.evilbeast.meizi.utils.GlideUtil;
import com.evilbeast.meizi.utils.ImmersiveUtil;
import com.evilbeast.meizi.utils.MeiZiUtil;
import com.evilbeast.meizi.utils.StatusBarCompat;
import com.jakewharton.rxbinding.view.RxMenuItem;
import com.tbruyelle.rxpermissions.RxPermissions;

import java.io.File;
import java.util.List;

import butterknife.BindView;
import io.realm.Realm;
import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MeiziDetailActivity extends RxBaseActivity {

    private static final String EXTRA_GROUP_ID = "extra_group_id";
    private static final String EXTRA_GROUP_TITLE = "extra_group_title";

    private int groupId;
    private String groupTitle;
    private String currentImageUrl;

    // 数据
    private Realm realm;
    private List<MeiZiGroup> mDataList;

    private MeiziDetailPagerAdapter mPagerAdapter;

    private boolean toolbarIsHidden;

    @BindView(R.id.appbar_layout)
    AppBarLayout mAppbarLayout;

    @BindView(R.id.toolbar)
    Toolbar mToolbar;

    @BindView(R.id.view_pager)
    ViewPager mViewPager;

    @Override
    public int getLayoutId() {
        return R.layout.meizi_detail_activity;
    }

    @Override
    public void initViews(Bundle savedInstanceState) {
        Intent intent = getIntent();
        groupId = intent.getIntExtra(EXTRA_GROUP_ID, -1);
        groupTitle = intent.getStringExtra(EXTRA_GROUP_TITLE);

        // 获取数据
        realm = Realm.getDefaultInstance();
        mDataList = realm.where(MeiZiGroup.class)
                .equalTo("groupId", groupId)
                .findAll();

        if (mDataList.size() > 0) {
            currentImageUrl = mDataList.get(0).getImageUrl();
        }

        mPagerAdapter = new MeiziDetailPagerAdapter(getSupportFragmentManager());
        mViewPager.setAdapter(mPagerAdapter);
        mPagerAdapter.notifyDataSetChanged();
        // mViewPager.setPageTransformer();
        // TODO: 2016/9/29 页面翻页过渡效果

        mViewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                currentImageUrl = mDataList.get(position).getImageUrl();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        RxBus.getInstance().toObserverable(String.class)
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        toggleToolbar();
                    }
                });
    }

    public static Intent newIntent(Activity activity, int groupId, String groupTitle) {
        Intent intent = new Intent(activity, MeiziDetailActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.putExtra(EXTRA_GROUP_ID, groupId);
        intent.putExtra(EXTRA_GROUP_TITLE, groupTitle);
        return intent;
    }


    @Override
    protected void onDestroy() {
        realm.close();
        super.onDestroy();
    }

    @Override
    public void initToolbar() {
        mToolbar.setTitle(groupTitle);
        mToolbar.setBackgroundResource(R.color.black_90);
        mAppbarLayout.setBackgroundResource(R.color.black_90);
        mAppbarLayout.setAlpha(0.5f);

        StatusBarCompat.setStatusBarColor(this, ContextCompat.getColor(this,R.color.black_90),50);

        mToolbar.setNavigationIcon(R.drawable.ic_back);
        mToolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                supportFinishAfterTransition();
            }
        });

        mToolbar.inflateMenu(R.menu.menu_meizi);

        // 保存图片
        connectSaveImageEvent();


    }

    private void connectSaveImageEvent() {
        RxMenuItem.clicks(mToolbar.getMenu().findItem(R.id.action_save_image))
                .compose(this.<Void>bindToLifecycle())
                .compose(RxPermissions.getInstance(MeiziDetailActivity.this).ensure(Manifest.permission.WRITE_EXTERNAL_STORAGE))
                .observeOn(Schedulers.io())
                .filter(new Func1<Boolean, Boolean>() {
                    @Override
                    public Boolean call(Boolean aBoolean) {
                        return aBoolean;
                    }
                })
                .flatMap(new Func1<Boolean, Observable<Uri>>() {
                    @Override
                    public Observable<Uri> call(Boolean aBoolean) {
                        return GlideUtil.saveImageToLocal(MeiziDetailActivity.this, currentImageUrl);
                    }
                })
                .map(new Func1<Uri, String>() {
                    @Override
                    public String call(Uri uri) {
                        return String.format("图片已保存至 %s 文件夹",
                                new File(Environment.getExternalStorageDirectory(), ConstantUtil.FILE_DIR)
                                        .getAbsolutePath());
                    }
                })
                .observeOn(AndroidSchedulers.mainThread())
                .retry()
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        Toast.makeText(MeiziDetailActivity.this, s, Toast.LENGTH_SHORT).show();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Toast.makeText(MeiziDetailActivity.this, "保存失败,请重试", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    protected void toggleToolbar() {
        if (toolbarIsHidden) {
            ImmersiveUtil.exit(this);
            mAppbarLayout.animate()
                    .translationY(0)
                    .setInterpolator(new DecelerateInterpolator(2))
                    .start();
            toolbarIsHidden = false;
        } else {
            ImmersiveUtil.enter(this);
            mAppbarLayout.animate()
                    .translationY(-mAppbarLayout.getHeight())
                    .setInterpolator(new DecelerateInterpolator(2))
                    .start();
            toolbarIsHidden = true;
        }
    }

    private class MeiziDetailPagerAdapter extends FragmentStatePagerAdapter {

        public MeiziDetailPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return MeiZiDetailPageFragment.newInstance(mDataList.get(position).getImageUrl());
        }

        @Override
        public int getCount() {
            return mDataList.size();
        }
    }
}
