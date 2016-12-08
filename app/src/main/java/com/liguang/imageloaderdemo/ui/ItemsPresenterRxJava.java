package com.liguang.imageloaderdemo.ui;

import android.content.Context;
import android.util.Log;

import com.liguang.imageloaderdemo.bean.ItemBean;
import com.liguang.imageloaderdemo.data.ItemsRepositoryRxJava;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class ItemsPresenterRxJava implements ItemsContract.Presenter {
    private static final String TAG = "ItemsPresenterRxJava";
    Context mContext;
    /**
     * 上层View
     */
    ItemsContract.View mView;
    /**
     * 底层Model
     */
    ItemsRepositoryRxJava mRepository;
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

    public ItemsPresenterRxJava() {

    }

    public ItemsPresenterRxJava(Context context, ItemsContract.View view, ItemsRepositoryRxJava repository, String type) {
        mContext = context;
        mView = view;
        mRepository = repository;
        mType = type;
        mView.setPresenter(this);
        mSubscriptions = new CompositeSubscription();
    }

    public void loadItems(boolean forceUpdate) {
        mPage++;
        if (forceUpdate || mFirstLoad) {
            mPage = 1;
        }
        mFirstLoad = false;
        mView.showLoading(true);
        mSubscriptions.clear();
        //下层负责创建Observable
        Subscription subscription = mRepository.getItems(mType, mPage)
                .flatMap(new Func1<List<ItemBean>, Observable<ItemBean>>() {
                    @Override
                    public Observable<ItemBean> call(List<ItemBean> beanList) {
                        return Observable.from(beanList);
                    }
                })
                .map(new Func1<ItemBean, ItemBean>() {
                    @Override
                    public ItemBean call(ItemBean itemBean) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
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
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        Log.d(TAG, "call: doOnCompleted load local database if needed?");
                    }
                })
                .subscribe(new Subscriber<List<ItemBean>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted: type = " + mType + " page = " + mPage);
                        mView.showLoading(false);
                    }

                    @Override
                    public void onError(Throwable e) {

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

    private void processItems(List<ItemBean> beanList) {
        if (beanList.isEmpty()) {
            processEmptyItems();
        } else {
            mView.showRecyclerView(true);
            mView.showItems(beanList);
        }
    }

    private void processEmptyItems() {
        mView.showNoItems();
    }

    @Override
    public void subscribe() {
        loadItems(false);
    }

    @Override
    public void unsubscribe() {
        //clear reference avoid memory leak
        mSubscriptions.clear();
    }
}
