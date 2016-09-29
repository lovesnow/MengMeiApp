package com.evilbeast.meizi.module.meizi;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.evilbeast.meizi.R;
import com.evilbeast.meizi.base.RxBaseFragment;
import com.evilbeast.meizi.rx.RxBus;


import butterknife.BindView;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * Author: sumary
 */
public class MeiZiDetailPageFragment extends RxBaseFragment implements RequestListener<String, GlideDrawable> {

    private static final String EXTRA_URL = "extra_url";
    private String image_url;

    private PhotoViewAttacher mPhotoViewAttacher;

    @BindView(R.id.image_view)
    ImageView mImageView;

    @BindView(R.id.error_msg)
    TextView mTextViewErrorMsg;

    @Override
    public int getLayoutId() {
        return R.layout.meizi_detail_page_fragment;
    }

    @Override
    public void initViews() {
        image_url = getArguments().getString(EXTRA_URL);
        Log.w("image", image_url);
        Glide.with(this).load(image_url)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .crossFade(0)
                .listener(this)
                .into(Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL);
    }

    @Override
    public boolean onException(Exception e, String model, Target<GlideDrawable> target, boolean isFirstResource) {
        // 显示错误信息
        mTextViewErrorMsg.setVisibility(View.VISIBLE);
        return false;
    }

    @Override
    public boolean onResourceReady(GlideDrawable resource, String model, Target<GlideDrawable> target, boolean isFromMemoryCache, boolean isFirstResource) {

        // 设置Image
        mImageView.setImageDrawable(resource);
        mPhotoViewAttacher = new PhotoViewAttacher(mImageView);
        setPhotoViewAttacher();

        // 关闭错误提示
        mTextViewErrorMsg.setVisibility(View.GONE);
        return false;
    }

    private void setPhotoViewAttacher() {
        mPhotoViewAttacher.setOnViewTapListener(new PhotoViewAttacher.OnViewTapListener() {
            @Override
            public void onViewTap(View view, float v, float v1) {
                RxBus.getInstance().post("toggleToolBar");
            }
        });
    }

    public static MeiZiDetailPageFragment newInstance(String url) {
        Bundle bundle = new Bundle();
        bundle.putString(EXTRA_URL, url);
        MeiZiDetailPageFragment fragment = new MeiZiDetailPageFragment();
        fragment.setArguments(bundle);
        return fragment;
    }
}
