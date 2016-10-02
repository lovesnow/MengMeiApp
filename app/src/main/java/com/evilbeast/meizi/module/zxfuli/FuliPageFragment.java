package com.evilbeast.meizi.module.zxfuli;

import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.view.View;

import com.evilbeast.meizi.R;
import com.evilbeast.meizi.adapter.AbstractAdapter;
import com.evilbeast.meizi.adapter.FuliAdapter;
import com.evilbeast.meizi.base.RxBaseFragment;
import com.evilbeast.meizi.entity.fuli.FuliItemObject;
import com.evilbeast.meizi.network.Api.ZxFuliApi;
import com.evilbeast.meizi.network.RetrofixHelper;
import com.evilbeast.meizi.utils.LogUtil;
import com.evilbeast.meizi.utils.MeiZiUtil;

import java.io.IOException;
import java.util.List;

import butterknife.BindView;
import io.realm.Realm;
import okhttp3.ResponseBody;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

import static com.evilbeast.meizi.adapter.AbstractAdapter.*;

/**
 * Author: sumary
 */
public class FuliPageFragment extends RxBaseFragment {
    private List<FuliItemObject> mDataList;
    private Realm realm;

    private FuliAdapter mAdapter;
    private StaggeredGridLayoutManager mLayoutManager;

    @BindView(R.id.fuli_recycler_view)
    RecyclerView mRecyclerView;

    @BindView(R.id.fuli_swipe_refresh)
    SwipeRefreshLayout mSwipeRefresh;

    private int page = 1;
    private int pageNum = 15;
    private int PRELOAD_NUM = 6;
    private boolean hasLoadMore = true;



    @Override
    public int getLayoutId() {
        return R.layout.fuli_page_fragment;
    }

    @Override
    public void initViews() {

        realm = Realm.getDefaultInstance();
        mDataList = realm.where(FuliItemObject.class).findAll();

        initRecyclerView();
        initSwipeRefresh();

    }

    private void initRecyclerView() {
        mRecyclerView.setHasFixedSize(true);
        mLayoutManager = new StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL);
        mRecyclerView.setLayoutManager(mLayoutManager);

        mAdapter = new FuliAdapter(mRecyclerView, mDataList);
        mRecyclerView.setAdapter(mAdapter);

        mRecyclerView.addOnScrollListener(loadMoreListener());
    }

    private RecyclerView.OnScrollListener loadMoreListener() {
        return new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                boolean isBottom = mLayoutManager.findLastCompletelyVisibleItemPositions(new int[2])[1] > mAdapter.getItemCount() - PRELOAD_NUM;
                if (!mSwipeRefresh.isRefreshing() && isBottom && hasLoadMore) {
                    mSwipeRefresh.setRefreshing(true);
                    page++;
                    fetchData();
                }

            }
        };
    }

    private void initSwipeRefresh() {
        mSwipeRefresh.setColorSchemeResources(R.color.colorAccent);
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                pullRefresh();
            }
        });

        mSwipeRefresh.postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefresh.setRefreshing(true);
                pullRefresh();
            }
        }, 500);
    }

    private void pullRefresh() {
        page = 1;
        hasLoadMore = true;
        clearData();
        fetchData();
    }

    private void clearData() {
        realm.executeTransaction(new Realm.Transaction() {
            @Override
            public void execute(Realm realm) {
                realm.where(FuliItemObject.class).findAll().deleteAllFromRealm();
            }
        });
    }


    private void fetchData() {
        RetrofixHelper.createTextApi(ZxFuliApi.class)
                .getImageData(page)
                .compose(this.<ResponseBody>bindToLifecycle())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Action1<ResponseBody>() {
                    @Override
                    public void call(ResponseBody responseBody) {
                        try {
                            String html = responseBody.string();
                            List<FuliItemObject> list = MeiZiUtil.getInstance().parseFuliItems(html);
                            MeiZiUtil.getInstance().putFuliItemCache(list);
                            boolean isEnd = list.size() <= 0;
                            finishTask(isEnd);

                        } catch (IOException e) {
                            if ( mSwipeRefresh.isRefreshing()) {
                                mSwipeRefresh.setRefreshing(false);
                            }
                            e.printStackTrace();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        if ( mSwipeRefresh.isRefreshing()) {
                            mSwipeRefresh.setRefreshing(false);
                        }
                    }
                });
    }

    private void finishTask(boolean isEnd) {
        if ( mSwipeRefresh.isRefreshing()) {
            mSwipeRefresh.setRefreshing(false);
        }

        if (isEnd) {
            hasLoadMore = false;

        } else {
            if (page > 1) {
                mAdapter.notifyItemRangeChanged((page-1)*pageNum-1, pageNum);
            } else {
                mAdapter.notifyDataSetChanged();
            }
        }

    }

    @Override
    public void onDestroy() {
        realm.close();
        super.onDestroy();
    }

    public static FuliPageFragment newInstance() {
        return new FuliPageFragment();
    }
}
