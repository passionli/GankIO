package com.liguang.gankio.ui;

import android.content.Context;
import android.util.Log;

import com.liguang.gankio.R;
import com.liguang.gankio.bean.ItemBean;
import com.liguang.gankio.data.ItemsRepository;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class ItemsPresenter implements ItemsContract.Presenter {
    private static final String TAG = "ItemsPresenter";
    Context mContext;
    /**
     * 上层View
     */
    ItemsContract.View mView;
    /**
     * 底层Model
     */
    ItemsRepository mRepository;
    /**
     * 类型，如"Android","iOS","前端"
     */
    private String mType;
    /**
     * 当前页
     */
    private int mPage = 1;

    private CompositeSubscription mSubscriptions;
    private boolean mFirstLoad = true;
    private List<ItemBean> mData;
    private int mNewItemCount;


    public ItemsPresenter() {

    }

    public ItemsPresenter(Context context, ItemsContract.View view, ItemsRepository repository, String type) {
        mContext = context;
        mView = view;
        mRepository = repository;
        mType = type;
        mView.setPresenter(this);
        mSubscriptions = new CompositeSubscription();
    }

    public void loadItems(boolean forceUpdate) {
        Log.d(TAG, "loadItems: forceUpdate = " + forceUpdate);

        mPage++;
        if (forceUpdate || mFirstLoad) {
            mPage = 1;
        }
        mFirstLoad = false;

        if (forceUpdate) {
            //头部显示正在加载
            mView.showHeaderRefreshing();
            mView.hideFooterLoading();
            mRepository.refreshItems();
        }
        if (mPage != 1) {
            mView.showFooterLoading();
        }

        getItemsInternal(false);
    }

    private void getItemsInternal(boolean fromLocalOnly) {
        mSubscriptions.clear();
        mNewItemCount = 0;
        //下层负责创建Observable
        Subscription subscription = mRepository.getItems(mType, mPage, fromLocalOnly)
                .flatMap(new Func1<List<ItemBean>, Observable<ItemBean>>() {
                    @Override
                    public Observable<ItemBean> call(List<ItemBean> beanList) {
                        return Observable.from(beanList);
                    }
                })
                .map(new Func1<ItemBean, ItemBean>() {
                    @Override
                    public ItemBean call(ItemBean itemBean) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.CHINA);
                        try {
                            itemBean.publishedAt = sdf.format(sdf.parse(itemBean.publishedAt));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return itemBean;
                    }
                })
                .toList()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<ItemBean>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted: type = " + mType + " page = " + mPage);
                        mView.hideHeaderRefreshing();
                        if (mNewItemCount == 0) {
                            //此次无新数据，UI显示没有更多数据了？
                            mView.showNoMoreItems();
                        } else {
                            mView.hideFooterLoading();
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: " + e.toString());
                        mView.showError();
                    }

                    @Override
                    public void onNext(List<ItemBean> beanList) {
                        //这里应该会有两步：1. 数据库 2.网络
                        Log.d(TAG, "onNext: ");
                        processItems(beanList);
                    }
                });
        mSubscriptions.add(subscription);
    }

    @Override
    public void reloadItemsFromLocal() {
        mPage = 1;
        getItemsInternal(true);
    }

    private void processItems(List<ItemBean> beanList) {
        if (beanList.isEmpty()) {
            processEmptyItems();
        } else {
            //merge array
            if (mData == null) {
                mData = beanList;
                mNewItemCount = beanList.size();
            } else {
                //O(n^2) ?
                for (ItemBean bean : beanList) {
                    if (!mData.contains(bean)) {
                        mData.add(bean);
                        mNewItemCount++;
                    }
                }
            }
            Log.d(TAG, "bindData: " + String.format(mContext.getString(R.string.item_new_load), mNewItemCount));
//            mNoMoreData = (mNewItemCount == 0);
            mView.showRecyclerView();
            mView.showItems(mData);
        }
    }

    private void processEmptyItems() {
        mView.showNoMoreItems();
    }

    @Override
    public void subscribe() {
        Log.d(TAG, "subscribe: ");
        //如果Presenter层已经加载过数据，则不需要重复加载
        if (mFirstLoad)
            loadItems(false);
    }

    @Override
    public void unsubscribe() {
        Log.d(TAG, "unsubscribe: ");
        //clear reference avoid memory leak
        mSubscriptions.clear();
    }
}
