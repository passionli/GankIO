package com.liguang.imageloaderdemo.data;

import android.support.annotation.NonNull;
import android.util.Log;

import com.liguang.imageloaderdemo.bean.ItemBean;

import java.util.List;

import rx.Observable;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;

public class ItemsRepository implements ItemsDataSource {
    private static final String TAG = "ItemsRepository";
    private static ItemsRepository INSTANCE = null;
    private final ItemsDataSource mItemsRemoteDataSource;
    private final ItemsDataSource mItemsLocalDataSource;

    private ItemsRepository(ItemsDataSource itemsRemoteDataSource, ItemsDataSource itemsLocalDataSource) {
        mItemsRemoteDataSource = itemsRemoteDataSource;
        mItemsLocalDataSource = itemsLocalDataSource;
    }

    public static ItemsRepository getInstance(ItemsDataSource itemsRemoteDataSource, ItemsDataSource itemsLocalDataSource) {
        if (INSTANCE == null) {
            INSTANCE = new ItemsRepository(itemsRemoteDataSource, itemsLocalDataSource);
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
            //两路并行，本地和服务器数据回来后都会向上汇报
            return Observable.merge(localItems, remoteItems);
        } else {
            return Observable.concat(localItems, remoteItems)
                    //这里使用takeFirst代替First
                    .takeFirst(new Func1<List<ItemBean>, Boolean>() {
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
                        Log.d(TAG, "call: repository data from remote is saving to local");
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
