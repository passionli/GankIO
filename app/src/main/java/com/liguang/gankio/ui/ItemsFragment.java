package com.liguang.gankio.ui;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.FrameLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.aspsine.irecyclerview.IRecyclerView;
import com.aspsine.irecyclerview.OnLoadMoreListener;
import com.aspsine.irecyclerview.OnRefreshListener;
import com.aspsine.irecyclerview.demo.ui.widget.footer.LoadMoreFooterView;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.samples.apps.iosched.util.ThrottledContentObserver;
import com.liguang.gankio.LGViewUtils;
import com.liguang.gankio.R;
import com.liguang.gankio.album.Utils;
import com.liguang.gankio.bean.ItemBean;
import com.liguang.gankio.db.GankIoContract;
import com.liguang.gankio.network.URLHelper;
import com.liguang.gankio.util.Injection;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;
import hugo.weaving.DebugLog;

public class ItemsFragment extends Fragment implements ItemsContract.View,
        OnRefreshListener, OnLoadMoreListener, ThrottledContentObserver.Callbacks {
    private static final String TAG = "ItemsFragment";
    private static final String EXTRA_TAG = "extra_tag";
    @BindView(R.id.tvEmpty)
    TextView mEmptyView;
    @BindView(R.id.stub)
    ViewStub mViewStub;
    protected IRecyclerView mRecyclerView;
    private LoadMoreFooterView mLoadMoreFooterView;
    private LinearLayoutManager mLayoutManager;
    private MyAdapter mAdapter;
    private int mImageWidth;
    private int mImageHeight;
    Unbinder mUnbinder;
    /**
     * Low level
     */
    ItemsContract.Presenter mPresenter;
    private CustomDividerDrawable mDividerDrawable;
    private boolean mRecyclerViewBusy;

    private final ContentObserver mContentObserver = new ThrottledContentObserver(this);

    public ItemsFragment() {
    }

    public static ItemsFragment newInstance(String tag) {
        ItemsFragment fragment = new ItemsFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_TAG, tag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void setPresenter(ItemsContract.Presenter presenter) {
        mPresenter = presenter;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
//        PreferenceManager.getDefaultSharedPreferences(context).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        if (getArguments() != null) {
            String type = getArguments().getString(EXTRA_TAG);
            mImageWidth = LGViewUtils.getScreenWidth(getActivity()) - LGViewUtils.dp2px(getActivity(), 46);
            mImageHeight = LGViewUtils.dp2px(getActivity(), 225);
            mPresenter = new ItemsPresenter(getActivity(), this, Injection.provideItemsRepository(getActivity().getApplicationContext()),
                    type);
        }
    }

    @DebugLog
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_item_list, container, false);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (isVisibleToUser && isResumed()) {
            onVisible();
        }
    }

    @DebugLog
    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        mUnbinder = ButterKnife.bind(this, view);
        mDividerDrawable = new CustomDividerDrawable(Color.parseColor("#F2F4F7"));
        mDividerDrawable.setIntrinsicHeight(LGViewUtils.dp2px(getActivity(), 10));
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new MyAdapter();
    }

    @DebugLog
    private void initView() {
        if (mViewStub == null)
            return;

        //only initView once
        mViewStub.inflate();
        mViewStub = null;

        //butter knife ?
        mRecyclerView = (IRecyclerView) getView().findViewById(R.id.iRecyclerView);
        mRecyclerView.setLayoutManager(mLayoutManager);
        mLoadMoreFooterView = (LoadMoreFooterView) mRecyclerView.getLoadMoreFooterView();
        mLoadMoreFooterView.setVisibility(View.GONE);
        mRecyclerView.setOnRefreshListener(this);
        mRecyclerView.setOnLoadMoreListener(this);
        mRecyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            public int mLastState;

            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE || newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    mRecyclerViewBusy = false;
                } else {
                    mRecyclerViewBusy = true;
                }

                if (mLastState == RecyclerView.SCROLL_STATE_SETTLING
                        && newState == RecyclerView.SCROLL_STATE_IDLE) {
                    //只在从Fling到Idle后，触发最后的一次BindView
                    mAdapter.notifyDataSetChanged();
                }
                mLastState = newState;
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        DividerItemDecoration itemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                mLayoutManager.getOrientation());
        itemDecoration.setDrawable(mDividerDrawable);
        mRecyclerView.addItemDecoration(itemDecoration);
        if (Utils.hasMarshmallow()) {
            String[] perms = {"android.permission.WRITE_EXTERNAL_STORAGE"};

            int permsRequestCode = 200;

            requestPermissions(perms, permsRequestCode);
        } else {
            mRecyclerView.setIAdapter(mAdapter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!getUserVisibleHint())
            return;
        onVisible();
    }

    @DebugLog
    private void onVisible() {
        getActivity().getContentResolver().registerContentObserver(GankIoContract.Item.CONTENT_URI, false, mContentObserver);
        mPresenter.subscribe();
    }

    @Override
    public void onPause() {
        super.onPause();
        if (getUserVisibleHint()) {
            mPresenter.unsubscribe();
            getActivity().getContentResolver().unregisterContentObserver(mContentObserver);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void showFooterLoading() {
        mLoadMoreFooterView.setStatus(LoadMoreFooterView.Status.LOADING);
    }

    @Override
    public void hideFooterLoading() {
        mLoadMoreFooterView.setStatus(LoadMoreFooterView.Status.GONE);
    }

    @Override
    public void showHeaderRefreshing() {
        mRecyclerView.setRefreshing(true);
    }

    @Override
    public void hideHeaderRefreshing() {
        initView();
        mRecyclerView.setRefreshing(false);
    }

    @Override
    public void showItems(List<ItemBean> beanList) {
        mAdapter.swapData(beanList);
    }

    @Override
    public void showNoMoreItems() {
        initView();
        mLoadMoreFooterView.setStatus(LoadMoreFooterView.Status.THE_END);
    }

    @Override
    public void showError() {
        initView();
        mLoadMoreFooterView.setStatus(LoadMoreFooterView.Status.ERROR);
    }

    @Override
    public void showRecyclerView() {
        mEmptyView.setVisibility(View.GONE);
    }

    @Override
    public void hideRecyclerView() {

    }

    @Override
    public void onRefresh() {
        mPresenter.loadItems(true);
    }

    @Override
    public void onLoadMore() {
        mPresenter.loadItems(false);
    }

    @DebugLog
    @Override
    public void onThrottledContentObserverFired() {
        //reload data
        mPresenter.loadItems(true);
    }

    private class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_ITEM = 0;
        //        private static final int TYPE_FOOTER = 1;
        LayoutInflater mInflater;
        private int mTitlePaddingTop;
        public List<ItemBean> mData;

        public MyAdapter() {
            mInflater = LayoutInflater.from(getActivity());
            mTitlePaddingTop = LGViewUtils.dp2px(getActivity(), 23);
            mData = new ArrayList<>();
        }

        public void swapData(List<ItemBean> data) {
            mData = data;
            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_ITEM) {
                View view = mInflater.inflate(R.layout.fragment_gank_list_item, null);
                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                return new MyViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                MyViewHolder viewHolder = (MyViewHolder) holder;
                final ItemBean bean = mData.get(position);
                if (bean.images == null) {
                    viewHolder.draweeView.setVisibility(View.GONE);
                    viewHolder.titleTv.setPadding(0, 0, 0, mTitlePaddingTop);
                } else {
                    viewHolder.draweeView.setVisibility(View.VISIBLE);
                    viewHolder.titleTv.setPadding(0, mTitlePaddingTop, 0, mTitlePaddingTop);
                }

                viewHolder.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String url = bean.url;
                        Intent intent = new Intent(getActivity(), GankDetailActivity.class);
                        intent.putExtra(GankDetailActivity.EXTRA_URL, url);
                        startActivity(intent);
                    }
                });

                //TODO 1. 在主线程给图片设置占位符 2. 在IDLE时向下层提交异步任务
                Log.d(TAG, "onBindViewHolder: mRecyclerViewBusy = " + mRecyclerViewBusy);
                if (!mRecyclerViewBusy && bean.images != null) {
                    Uri uri = Uri.parse(URLHelper.createImageUrlWithWidth(bean.images[0], mImageWidth));
                    ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(uri)
                            .setLocalThumbnailPreviewsEnabled(true)
                            .setResizeOptions(new ResizeOptions(mImageWidth, mImageHeight)).build();
                    DraweeController draweeController = Fresco.newDraweeControllerBuilder()
                            .setOldController(((MyViewHolder) holder).draweeView.getController())
                            .setImageRequest(imageRequest)
                            .build();
                    ((MyViewHolder) holder).draweeView.setController(draweeController);
                    GenericDraweeHierarchyBuilder builder =
                            new GenericDraweeHierarchyBuilder(getResources());
                    //在代码中设置比在XML中设置有效，更多情况下请使用代码设置！
                    GenericDraweeHierarchy hierarchy = builder
                            .setFadeDuration(300)
//                    .setPlaceholderImage(R.mipmap.thumb_picture_loading)
                            .setFailureImage(R.mipmap.thumb_load_fail)
                            .build();
                    ((MyViewHolder) holder).draweeView.setHierarchy(hierarchy);
                } else {
                    viewHolder.draweeView.setImageDrawable(null);
                }
                viewHolder.titleTv.setText(bean.desc);
                if (!TextUtils.isEmpty(bean.who)) {
                    if (viewHolder.whoTv.getVisibility() != View.VISIBLE) {
                        viewHolder.whoTv.setVisibility(View.VISIBLE);
                    }
                    viewHolder.whoTv.setText(String.format("via %s", bean.who));
                } else {
                    viewHolder.whoTv.setVisibility(View.GONE);
                }
                viewHolder.publishedAtTv.setText(bean.publishedAt);
            }
        }

        @Override
        public int getItemCount() {
            if (mData != null)
                return mData.size();
            else
                return 0;
        }

        @Override
        public int getItemViewType(int position) {
            // 最后一个item设置为footerView
//            if (position + 1 == getItemCount()) {
//                return TYPE_FOOTER;
//            } else {
            return TYPE_ITEM;
//            }
        }
    }

    public class MyViewHolder extends RecyclerView.ViewHolder {
        private View.OnClickListener mOnClickListener;
        @BindView(R.id.titleTv)
        TextView titleTv;
        @BindView(R.id.whoTv)
        TextView whoTv;
        @BindView(R.id.publishedAtTv)
        TextView publishedAtTv;
        @BindView(R.id.draweeView)
        DraweeView draweeView;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        void setOnClickListener(View.OnClickListener onClickListener) {
            mOnClickListener = onClickListener;
        }

        @OnClick(R.id.container)
        void onClick(View view) {
            mOnClickListener.onClick(view);
        }
    }

    class FooterViewHolder {
        @BindView(R.id.container)
        FrameLayout container;
        @BindView(R.id.progressBar)
        ProgressBar progressBar;
        @BindView(R.id.loadMoreTv)
        TextView loadMoreTv;

        public FooterViewHolder(View view) {
            ButterKnife.bind(this, view);
        }
    }
}
