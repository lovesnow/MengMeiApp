package com.evilbeast.meizi.module.meizi;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.MotionEvent;
import android.view.View;

import com.evilbeast.meizi.R;
import com.evilbeast.meizi.adapter.AbstractAdapter;
import com.evilbeast.meizi.adapter.MeiZiAdapter;
import com.evilbeast.meizi.base.RxBaseFragment;
import com.evilbeast.meizi.entity.meizi.MeiZi;
import com.evilbeast.meizi.entity.meizi.MeiZiGroup;
import com.evilbeast.meizi.network.Api.MeiZiApi;
import com.evilbeast.meizi.network.RetrofixHelper;
import com.evilbeast.meizi.utils.MeiZiUtil;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import io.realm.Realm;
import okhttp3.ResponseBody;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

/**
 * Author: sumary
 */
public class MeiZiSimpleFragment extends RxBaseFragment {

    public static final String EXTRA_TYPE = "extra_type";

    // 获取Views
    @BindView(R.id.recycler)
    RecyclerView mRecyclerView;

    @BindView(R.id.swipe_refresh)
    SwipeRefreshLayout mSwipeRefreshLayout;

    private StaggeredGridLayoutManager mLayoutManager;
    private MeiZiAdapter mAdapter;
    private List<MeiZi> mDataList;
    private int page = 1;
    private String cateType;
    private int pageNum = 24;
    private Realm realm;
    private int PRELOAD_SIZE = 6;
    private boolean hasLoadMore = true;


    @Override
    public int getLayoutId() {
        return R.layout.meizi_category_fragment;
    }

    @Override
    public void initViews() {

        // init Swipe refresh
        initSwipeRefresh();

        cateType = getArguments().getString(EXTRA_TYPE);
        realm = Realm.getDefaultInstance();

        // 加载数据
        mDataList = realm.where(MeiZi.class)
                .equalTo("type", cateType)
                .findAll();

        // init RecyclerView
        initRecyclerView();

        // TODO: 2016/9/28 Tab


    }

    private void initRecyclerView() {

        // 设置每个item大小一样
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        // 底部加载
        mRecyclerView.addOnScrollListener(OnLoadMoreListener(mLayoutManager));


        // 设置Adapter
        mAdapter = new MeiZiAdapter(mRecyclerView, mDataList);
        mRecyclerView.setAdapter(mAdapter);

        //setRecycleScrollBug();
    }

    private void initSwipeRefresh() {
        mSwipeRefreshLayout.setColorSchemeResources(R.color.colorPrimary);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pull_refresh_data();
            }
        });

        // 首次打开的时候加载数据
        mSwipeRefreshLayout.postDelayed(new Runnable() {
            @Override
            public void run() {
                pull_refresh_data();
            }
        }, 500);
    }

    private void pull_refresh_data() {
        mSwipeRefreshLayout.setRefreshing(true);
        page = 1;
        if (cateType.equals("best")) {
            hasLoadMore = false;
        } else {
            hasLoadMore = true;
        }
        clearCache();
        fetchAndSaveData();
    }

    private void fetchGroupDataAndOpen(final int groupId, final String groupTitle)  {
        RetrofixHelper.createTextApi(MeiZiApi.class)
                .getGroupImages(groupId)
                .compose(this.<ResponseBody>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody>() {
                    @Override
                    public void call(ResponseBody responseBody) {
                        try {
                            List<MeiZiGroup> list = MeiZiUtil.getInstance().parseMeiZiGroupSave(responseBody.string(), groupId);
                            MeiZiUtil.getInstance().putGroupCache(list);
                            Intent intent = MeiziDetailActivity.newIntent(getActivity(), groupId, groupTitle);
                            startActivity(intent);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                    }
                });
    }

    private void fetchAndSaveData() {
        RetrofixHelper.createTextApi(MeiZiApi.class)
                .getMeiziData(cateType, page)
                .compose(this.<ResponseBody>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody>() {
                    @Override
                    public void call(ResponseBody responseBody) {
                        try {
                            String html = responseBody.string();
                            List<MeiZi> list = MeiZiUtil.getInstance().parserMeiziTuHtml(html, cateType);
                            if (list.size() > 0) {
                                MeiZiUtil.getInstance().putMeiZiCache(list);
                                finishTask();
                            } else {
                                hasLoadMore = false;
                                // 关闭刷新指示器
                                if (mSwipeRefreshLayout.isRefreshing()) {
                                    mSwipeRefreshLayout.setRefreshing(false);
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {

                        mSwipeRefreshLayout.post(new Runnable() {
                            @Override
                            public void run() {
                                mSwipeRefreshLayout.setRefreshing(false);
                            }
                        });

                        // TODO: 2016/9/28 加载失败提示
                    }
                });
    }

    private void finishTask() {

        // 通知变化的数据
        if (page >= 2) {
            mAdapter.notifyItemRangeChanged((page - 1) * pageNum - 1, pageNum );
        } else {
            mAdapter.notifyDataSetChanged();
        }

        // 关闭刷新指示器
        if (mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
        }

        // TODO: 2016/9/28 设置itemclickListener
        mAdapter.setOnItemClickListener(new AbstractAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position, AbstractAdapter.ClickableViewHolder holder) {
                fetchGroupDataAndOpen(mDataList.get(position).getGroupid(), mDataList.get(position).getTitle());
            }
        });
    }


    private void clearCache() {
        realm.beginTransaction();
        realm.where(MeiZi.class)
                .equalTo("type", cateType)
                .findAll()
                .deleteAllFromRealm();
        realm.commitTransaction();
    }

    RecyclerView.OnScrollListener OnLoadMoreListener (StaggeredGridLayoutManager layoutManager) {
        return new RecyclerView.OnScrollListener() {

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                boolean isBottom = mLayoutManager.findLastCompletelyVisibleItemPositions(new int[2])[1] >= mAdapter.getItemCount() - PRELOAD_SIZE;
                if (!mSwipeRefreshLayout.isRefreshing() && isBottom && hasLoadMore) {
                    mSwipeRefreshLayout.setRefreshing(true);
                    page++;
                    fetchAndSaveData();
                }
            }
        };
    }

    private void setRecycleScrollBug()
    {

        mRecyclerView.setOnTouchListener(new View.OnTouchListener()
        {

            @Override
            public boolean onTouch(View v, MotionEvent event)
            {


                if (mSwipeRefreshLayout.isRefreshing())
                {
                    return true;
                } else
                {
                    return false;
                }
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        realm.close();
    }

    // 通类型参数生成实例
    public static MeiZiSimpleFragment newInstance(String type) {
        MeiZiSimpleFragment mFragment = new MeiZiSimpleFragment();
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_TYPE, type);
        mFragment.setArguments(bundle);
        return mFragment;
    }



}
