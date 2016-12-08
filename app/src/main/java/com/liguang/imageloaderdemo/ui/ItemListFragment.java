package com.liguang.imageloaderdemo.ui;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.generic.GenericDraweeHierarchy;
import com.facebook.drawee.generic.GenericDraweeHierarchyBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.DraweeView;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.google.samples.apps.iosched.util.ThrottledContentObserver;
import com.liguang.imageloaderdemo.LGViewUtils;
import com.liguang.imageloaderdemo.R;
import com.liguang.imageloaderdemo.album.Utils;
import com.liguang.imageloaderdemo.bean.ItemBean;
import com.liguang.imageloaderdemo.bean.ItemListBean;
import com.liguang.imageloaderdemo.dao.ItemListDao;
import com.liguang.imageloaderdemo.db.GankIoContract;
import com.liguang.imageloaderdemo.network.URLHelper;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import butterknife.Unbinder;

public class ItemListFragment extends Fragment implements SwipeRefreshLayout.OnRefreshListener {
    private static final String TAG = "ItemListFragment";
    private static final String EXTRA_TAG = "extra_tag";
    Unbinder mUnbinder;
    private ItemListBean mData;
    private String mTag;
    @BindView(R.id.swipeRefreshLayout)
    SwipeRefreshLayout mSwipeRefreshLayout;
    @BindView(R.id.recyclerView)
    protected RecyclerView mRecyclerView;
    private LinearLayoutManager mLayoutManager;
    private MyAdapter mAdapter;
    private FetchDataTask mFetchDataTask;
    private int mPage = 0;
    private int mImageWidth;
    private int mImageHeight;
    private int mLastVisibleItem;
    private boolean mLoading;
    private ThrottledContentObserver mSessionsObserver;

    public ItemListFragment() {
        // Required empty public constructor
    }

    public static ItemListFragment newInstance(String tag) {
        ItemListFragment fragment = new ItemListFragment();
        Bundle args = new Bundle();
        args.putString(EXTRA_TAG, tag);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Log.d(TAG, "onAttach: ");
        // Register content observers
        mSessionsObserver = new ThrottledContentObserver(new ThrottledContentObserver.Callbacks() {
            @Override
            public void onThrottledContentObserverFired() {
                fireReloadEvent();
            }
        });
        getContext().getContentResolver().registerContentObserver(GankIoContract.Item.CONTENT_URI, false, mSessionsObserver);
    }

    private void fireReloadEvent() {
        Log.d(TAG, "fireReloadEvent: ");
        if (!isAdded()) {
            return;
        }
//        for (UserActionListener h1 : mListeners) {
//            Bundle args = new Bundle();
//            args.putInt(PresenterFragmentImpl.KEY_RUN_QUERY_ID,
//                    ExploreModel.ExploreQueryEnum.SESSIONS.getId());
//            h1.onUserAction(ExploreModel.ExploreUserActionEnum.RELOAD, args);
//        }
        loadMoreData();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mTag = getArguments().getString(EXTRA_TAG);
        }
        mImageWidth = LGViewUtils.getScreenWidth(getContext()) - LGViewUtils.dp2px(getContext(), 46);
        mImageHeight = LGViewUtils.dp2px(getContext(), 225);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_item_list, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mUnbinder = ButterKnife.bind(this, view);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mSwipeRefreshLayout.setProgressBackgroundColorSchemeResource(android.R.color.white);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_blue_light,
                android.R.color.holo_red_light, android.R.color.holo_orange_light,
                android.R.color.holo_green_light);
        mSwipeRefreshLayout.setProgressViewOffset(false, 0, (int) TypedValue
                .applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources()
                        .getDisplayMetrics()));

        mRecyclerView = (RecyclerView) view.findViewById(R.id.recyclerView);
        mLayoutManager = new LinearLayoutManager(getActivity());
        mAdapter = new MyAdapter();
        mRecyclerView.setLayoutManager(mLayoutManager);
        mRecyclerView.setOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                if (newState == RecyclerView.SCROLL_STATE_IDLE || newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                    Fresco.getImagePipeline().resume();
                } else {
                    Fresco.getImagePipeline().pause();
                }

                if (newState == RecyclerView.SCROLL_STATE_IDLE && mLastVisibleItem + 1 == mAdapter.getItemCount()) {
                    loadMoreData();
                }
            }

            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);
                mLastVisibleItem = mLayoutManager.findLastVisibleItemPosition();
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
//            mImageLoader = ImageLoader.getInstance(getActivity());
            mRecyclerView.setAdapter(mAdapter);
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        //Stop -> Start: new Task instance to fetch data
        loadMoreData();
    }

    private void bindData(ItemListBean itemListBean) {
        int newItemCount = 0;
        //merge array
        if (mData == null) {
            mData = itemListBean;
            newItemCount = itemListBean.results.size();
        } else {
            for (ItemBean bean : itemListBean.results) {
                if (!mData.results.contains(bean)) {
                    mData.results.add(bean);
                    newItemCount++;
                }
            }
        }
//        Toast.makeText(getActivity(), String.format(getString(R.string.item_new_load), newItemCount), Toast.LENGTH_SHORT).show();
        Log.d(TAG, "bindData: " + String.format(getString(R.string.item_new_load), newItemCount));
        //这里应该从ContentProvider加载数据
        mAdapter.notifyDataSetChanged();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        mUnbinder.unbind();
    }

    @Override
    public void onDetach() {
        super.onDetach();
        Log.d(TAG, "onDetach: ");
        getActivity().getContentResolver().unregisterContentObserver(mSessionsObserver);
    }

    @Override
    public void onRefresh() {
        Log.d(TAG, "onRefresh: ");
        mPage = 0;
        //强行从服务器刷新数据
        loadMoreData();
    }

    private void loadMoreData() {
        if (mLoading)
            return;
        mLoading = true;
        mAdapter.notifyDataSetChanged();

        //TODO UI 线程发起两个异步调用到底层消息队列，一个本地，一个远程拉数据
        mPage++;
        if (mFetchDataTask != null) {
            mFetchDataTask.cancel(false);
        }
        mFetchDataTask = new FetchDataTask();
        mFetchDataTask.execute();
    }

    private class FetchDataTask extends AsyncTask<Void, Void, ItemListBean> {
        @Override
        protected void onPreExecute() {
            mLoading = true;
        }

        @Override
        protected ItemListBean doInBackground(Void... params) {
            ItemListDao dao = new ItemListDao(mTag, mPage);
            ItemListBean itemListBean = dao.getItems();
            return itemListBean;
        }

        @Override
        protected void onPostExecute(ItemListBean itemListBean) {
            //上层UI已经退出
            if (mSwipeRefreshLayout == null)
                return;
            mLoading = false;
            mSwipeRefreshLayout.setRefreshing(false);
            if (itemListBean != null)
                bindData(itemListBean);
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case 200:
                boolean cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED;
                if (cameraAccepted) {
                    //授权成功之后，调用系统相机进行拍照操作等
//                    mImageLoader = ImageLoader.getInstance(getContext());
                    mRecyclerView.setAdapter(mAdapter);
                } else {
                    //用户授权拒绝之后，友情提示一下就可以了
                }

                break;
        }
    }


    private class MyAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
        private static final int TYPE_ITEM = 0;
        private static final int TYPE_FOOTER = 1;
        LayoutInflater mInflater;
        private int mTitlePaddingTop;

        public MyAdapter() {
            mInflater = LayoutInflater.from(getContext());
            mTitlePaddingTop = LGViewUtils.dp2px(getContext(), 23);
        }

        @Override
        public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            if (viewType == TYPE_ITEM) {
                View view = mInflater.inflate(R.layout.activity_gank_list_item, null);
                view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT));
                return new MyViewHolder(view);
            }
            // type == TYPE_FOOTER 返回footerView
            else if (viewType == TYPE_FOOTER) {
                View view = mInflater.inflate(R.layout.footerview, null);
                view.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT));
                return new FooterViewHolder(view);
            }
            return null;
        }

        @Override
        public void onBindViewHolder(RecyclerView.ViewHolder holder, int position) {
            if (holder instanceof MyViewHolder) {
                MyViewHolder viewHolder = (MyViewHolder) holder;
                ItemBean bean = mData.results.get(position);
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
            } else if (holder instanceof FooterViewHolder) {
                FooterViewHolder footerViewHolder = (FooterViewHolder) holder;
                if (mLoading) {
                    footerViewHolder.progressBar.setVisibility(View.VISIBLE);
                    footerViewHolder.loadMoreTv.setText(R.string.loading);
                } else {
                    footerViewHolder.progressBar.setVisibility(View.GONE);
                    footerViewHolder.loadMoreTv.setText(R.string.loadMore);
                }
            }
        }

        @Override
        public int getItemCount() {
            if (mData != null)
                return mData.results.size() + 1;
            else
                return 1;
        }

        @Override
        public int getItemViewType(int position) {
            // 最后一个item设置为footerView
            if (position + 1 == getItemCount()) {
                return TYPE_FOOTER;
            } else {
                return TYPE_ITEM;
            }
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
//            adapter = new ImagePagerAdapter();
//            viewPager.setAdapter(adapter);
        }

        @OnClick(R.id.container)
        void startDetailActivity() {
            String url = mData.results.get(getAdapterPosition()).url;
            Intent intent = new Intent(getContext(), GankDetailActivity.class);
            intent.putExtra(GankDetailActivity.EXTRA_URL, url);
            startActivity(intent);
        }
    }

    class FooterViewHolder extends RecyclerView.ViewHolder {
        @BindView(R.id.container)
        FrameLayout container;
        @BindView(R.id.progressBar)
        ProgressBar progressBar;
        @BindView(R.id.loadMoreTv)
        TextView loadMoreTv;

        public FooterViewHolder(View view) {
            super(view);
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

//        public void setData(String[] imageUrls) {
//            mImageUrls = imageUrls;
//            notifyDataSetChanged();
//        }
//

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
