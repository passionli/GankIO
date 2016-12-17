package com.liguang.imageloaderdemo.ui;

import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
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
import android.widget.LinearLayout;
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
import com.liguang.imageloaderdemo.LGViewUtils;
import com.liguang.imageloaderdemo.R;
import com.liguang.imageloaderdemo.album.Utils;
import com.liguang.imageloaderdemo.bean.ItemBean;
import com.liguang.imageloaderdemo.network.URLHelper;
import com.liguang.imageloaderdemo.util.Injection;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ItemsFragment extends Fragment implements ItemsContract.View {
    private static final String TAG = "ItemsFragment";
    private static final String EXTRA_TAG = "extra_tag";
    @BindView(R.id.stub)
    ViewStub mViewStub;
//    @BindView(R.id.iRecyclerView)
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
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate: ");
        if (getArguments() != null) {
            String type = getArguments().getString(EXTRA_TAG);
            mImageWidth = LGViewUtils.getScreenWidth(getContext()) - LGViewUtils.dp2px(getContext(), 46);
            mImageHeight = LGViewUtils.dp2px(getContext(), 225);
            mPresenter = new ItemsPresenter(getContext(), this, Injection.provideTasksRepository(getContext().getApplicationContext()),
                    type);
        }
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_item_list, container, false);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        Log.d(TAG, "setUserVisibleHint: isVisibleToUser=" + isVisibleToUser);
        if (isVisibleToUser && isResumed()) {
            onVisible();
        } else {

        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated: ");
        mUnbinder = ButterKnife.bind(this, view);
    }

    private void initView() {
        Log.d(TAG, "initView: ");
        if (mViewStub == null)
            return;

        //only initView once
        mViewStub.inflate();
        mViewStub = null;

        //butterknife ?
        mRecyclerView = (IRecyclerView) getView().findViewById(R.id.iRecyclerView);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new MyAdapter();
        mRecyclerView.setLayoutManager(mLayoutManager);
        mLoadMoreFooterView = (LoadMoreFooterView) mRecyclerView.getLoadMoreFooterView();

        mRecyclerView.setOnRefreshListener(new OnRefreshListener() {
            @Override
            public void onRefresh() {
                Log.d(TAG, "onRefresh: ");
                mPresenter.loadItems(true);
            }
        });
        mRecyclerView.setOnLoadMoreListener(new OnLoadMoreListener() {
            @Override
            public void onLoadMore() {
                Log.d(TAG, "onLoadMore: ");
                mPresenter.loadItems(false);
            }
        });

        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE || newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    Fresco.getImagePipeline().resume();
                } else {
                    Fresco.getImagePipeline().pause();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
            }
        });
        DividerItemDecoration itemDecoration = new DividerItemDecoration(mRecyclerView.getContext(),
                mLayoutManager.getOrientation());
        CustomDividerDrawable drawable = new CustomDividerDrawable(Color.parseColor("#F2F4F7"));
        drawable.setIntrinsicHeight(LGViewUtils.dp2px(getContext(), 10));
        itemDecoration.setDrawable(drawable);
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
        Log.d(TAG, "onResume: ");
        if (!getUserVisibleHint())
            return;
        onVisible();
    }

    private void onVisible() {
        Log.d(TAG, "onVisible: ");
        initView();
        mPresenter.subscribe();
    }

    @Override
    public void onPause() {
        super.onPause();
        mPresenter.unsubscribe();
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
        mRecyclerView.setRefreshing(false);
    }

    @Override
    public void showItems(List<ItemBean> beanList) {
        Log.d(TAG, "showItems: ");
        mAdapter.swapData(beanList);
    }

    @Override
    public void showNoMoreItems() {
        mLoadMoreFooterView.setStatus(LoadMoreFooterView.Status.THE_END);
    }

    @Override
    public void showError() {
        mLoadMoreFooterView.setStatus(LoadMoreFooterView.Status.ERROR);
    }

    @Override
    public void showRecyclerView() {

    }

    @Override
    public void hideRecyclerView() {

    }

    private class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_ITEM = 0;
        //        private static final int TYPE_FOOTER = 1;
        LayoutInflater mInflater;
        private int mTitlePaddingTop;
        public List<ItemBean> mData;

        public MyAdapter() {
            mInflater = LayoutInflater.from(getContext());
            mTitlePaddingTop = LGViewUtils.dp2px(getContext(), 23);
            mData = new ArrayList<>();
        }

        public void swapData(List<ItemBean> data) {
            mData = data;
            notifyDataSetChanged();
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_ITEM) {
                View view = mInflater.inflate(R.layout.activity_gank_list_item, null);
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
                ItemBean bean = mData.get(position);
                if (bean.images == null) {
                    viewHolder.viewPager.setVisibility(View.GONE);
                    viewHolder.titleTv.setPadding(0, 0, 0, mTitlePaddingTop);
                } else {
                    viewHolder.viewPager.setVisibility(View.VISIBLE);
                    viewHolder.titleTv.setPadding(0, mTitlePaddingTop, 0, mTitlePaddingTop);
                }
                viewHolder.viewPager.setAdapter(new ImagePagerAdapter(bean.images));
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
        @BindView(R.id.container)
        LinearLayout container;
        @BindView(R.id.titleTv)
        TextView titleTv;
        @BindView(R.id.whoTv)
        TextView whoTv;
        @BindView(R.id.publishedAtTv)
        TextView publishedAtTv;
        @BindView(R.id.viewPager)
        ViewPager viewPager;
//        ImagePagerAdapter adapter;

        public MyViewHolder(View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        @OnClick(R.id.container)
        void startDetailActivity() {
//            String url = mData.results.get(getAdapterPosition()).url;
//            Intent intent = new Intent(getContext(), GankDetailActivity.class);
//            intent.putExtra(GankDetailActivity.EXTRA_URL, url);
//            startActivity(intent);
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

    private class ImagePagerAdapter extends PagerAdapter {
        private String[] mImageUrls;
        LayoutInflater mInflater;

        public ImagePagerAdapter(String[] imageUrls) {
            mInflater = LayoutInflater.from(getContext());
            mImageUrls = imageUrls;
        }

        @Override
        public int getCount() {
//            return 5;
            return mImageUrls == null ? 0 : mImageUrls.length;
        }

        @Override
        public boolean isViewFromObject(View view, Object object) {
            return view == object;
        }

        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            FrameLayout frameLayout = (FrameLayout) mInflater.inflate(R.layout.fragment_image_detail, null);
            DraweeView draweeView = (DraweeView) frameLayout.findViewById(R.id.my_image_view);
            draweeView.setLayoutParams(new FrameLayout.LayoutParams(mImageWidth,
                    mImageHeight));
//            Uri uri = Uri.parse("http://b.hiphotos.baidu.com/zhidao/pic/item/a6efce1b9d16fdfafee0cfb5b68f8c5495ee7bd8.jpg");
            Uri uri = Uri.parse(URLHelper.createImageUrlWithWidth(mImageUrls[position], mImageWidth));
            ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setLocalThumbnailPreviewsEnabled(true)
                    .setResizeOptions(new ResizeOptions(mImageWidth, mImageHeight)).build();
            DraweeController draweeController = Fresco.newDraweeControllerBuilder()
                    .setOldController(draweeView.getController())
                    .setImageRequest(imageRequest)
                    .build();
            draweeView.setController(draweeController);
            GenericDraweeHierarchyBuilder builder =
                    new GenericDraweeHierarchyBuilder(getResources());
            //在代码中设置比在XML中设置有效，更多情况下请使用代码设置！
            GenericDraweeHierarchy hierarchy = builder
                    .setFadeDuration(300)
//                    .setPlaceholderImage(R.mipmap.thumb_picture_loading)
                    .setFailureImage(R.mipmap.thumb_load_fail)
                    .build();
            draweeView.setHierarchy(hierarchy);

            container.addView(frameLayout);
            return frameLayout;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView((View) object);
        }
    }
}
