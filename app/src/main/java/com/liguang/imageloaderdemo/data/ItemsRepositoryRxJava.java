package com.liguang.imageloaderdemo.data;

import android.support.annotation.NonNull;
import android.util.Log;

import com.liguang.imageloaderdemo.bean.ItemBean;

import java.util.List;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

public class ItemsRepositoryRxJava implements ItemsDataSourceRxJava {
    private static final String TAG = "ItemsRepositoryRxJava";
    private static ItemsRepositoryRxJava INSTANCE = null;
    private final ItemsDataSourceRxJava mItemsRemoteDataSource;
    private final ItemsDataSourceRxJava mItemsLocalDataSource;

    private ItemsRepositoryRxJava(ItemsDataSourceRxJava itemsRemoteDataSource, ItemsDataSourceRxJava itemsLocalDataSource) {
        mItemsRemoteDataSource = itemsRemoteDataSource;
        mItemsLocalDataSource = itemsLocalDataSource;
    }

    public static ItemsRepositoryRxJava getInstance(ItemsDataSourceRxJava itemsRemoteDataSource, ItemsDataSourceRxJava itemsLocalDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new ItemsRepositoryRxJava(itemsRemoteDataSource, itemsLocalDataSource);
        }

        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    private void refreshLocalDataSource(List<ItemBean> items) {
        mItemsLocalDataSource.saveItems(items);
    }

    @Override
    public Observable<List<ItemBean>> getItems(String tag, int page) {
        Log.d(TAG, "getItems: ");
        Observable<List<ItemBean>> localItems = mItemsLocalDataSource.getItems(tag, page);
        Observable<List<ItemBean>> remoteItems = getAndSaveRemoteItems(tag, page);
        if (page == 1) {
            //这里需要加个强行刷新数据标志位?
            // 需要去服务器拉数据
            //两路并行，本地的数据先加载显示，等服务器数据回来后再次刷新界面
            return Observable.merge(localItems, remoteItems);
        } else {
            return Observable.concat(localItems, remoteItems).first(new Func1<List<ItemBean>, Boolean>() {
                @Override
                public Boolean call(List<ItemBean> beanList) {
                    Log.d(TAG, "call: beanList size = " + beanList.size());
                    //如果数据库查询够一页数据，则不用去服务器拉数据
                    return beanList.size() == 25;
                }
            });
        }
    }

    private Observable<List<ItemBean>> getAndSaveRemoteItems(String tag, int page) {
        return mItemsRemoteDataSource
                .getItems(tag, page)
                .map(new Func1<List<ItemBean>, List<ItemBean>>() {
                    @Override
                    public List<ItemBean> call(List<ItemBean> beanList) {
                        Log.d(TAG, "call: repository is pulling data from remote to local");
                        mItemsLocalDataSource.saveItems(beanList);
                        return beanList;
                    }
                })
                .doOnNext(new Action1<List<ItemBean>>() {
                    @Override
                    public void call(List<ItemBean> beanList) {
                        Log.d(TAG, "call: doOnNext");
                    }
                })
                .doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        Log.d(TAG, "call: doOnCompleted");
                    }
                })
                .doOnTerminate(new Action0() {
                    @Override
                    public void call() {
                        Log.d(TAG, "call: doOnTerminate");
                    }
                });
    }

    @Override
    public void saveItems(@NonNull List<ItemBean> items) {

    }
}
