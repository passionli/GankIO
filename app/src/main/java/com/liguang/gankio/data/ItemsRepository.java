package com.liguang.gankio.data;

import android.support.annotation.NonNull;
import android.util.Log;

import com.liguang.gankio.GankIOApplication;
import com.liguang.gankio.bean.ItemBean;
import com.liguang.gankio.config.AppConfig;
import com.liguang.gankio.util.Utils;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import hugo.weaving.DebugLog;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action0;
import rx.functions.Func1;

import static com.liguang.gankio.config.AppConfig.TAB_TAG;

public class ItemsRepository implements ItemsDataSource {
    private static final String TAG = "ItemsRepository";
    private static ItemsRepository INSTANCE = null;
    private final ItemsDataSource mItemsRemoteDataSource;
    private final ItemsDataSource mItemsLocalDataSource;
    //第一页数据缓存，线程安全
    private Map<String, List<ItemBean>> mFirstPageItems;
    private boolean mCacheIsDirty = false;

    //should not be called from outside
    private ItemsRepository(ItemsDataSource itemsRemoteDataSource, ItemsDataSource itemsLocalDataSource) {
        mItemsRemoteDataSource = itemsRemoteDataSource;
        mItemsLocalDataSource = itemsLocalDataSource;
        mFirstPageItems = new ConcurrentHashMap<>();
    }

    public static ItemsRepository getInstance(ItemsDataSource itemsRemoteDataSource, ItemsDataSource itemsLocalDataSource) {
        if (INSTANCE == null) {
            synchronized (ItemsRepository.class) {
                if (INSTANCE == null) {
                    INSTANCE = new ItemsRepository(itemsRemoteDataSource, itemsLocalDataSource);
                }
            }
        }

        return INSTANCE;
    }

    public static void destroyInstance() {
        INSTANCE = null;
    }

    private void refreshLocalDataSource(List<ItemBean> items) {
        mItemsLocalDataSource.saveItems(items);
    }

    @DebugLog
    @Override
    public Observable<List<ItemBean>> getItems(String tag, int page) {
        //memory
        if (page == 1) {
            List<ItemBean> cachedData = mFirstPageItems.get(tag);
            // Respond immediately with cache if available and not dirty
            if (cachedData != null && cachedData.size() > 0 && !mCacheIsDirty) {
                Log.d(TAG, "getItems: cache hit " + tag);
                return Observable.from(cachedData).toList();
            } else {
                Log.d(TAG, "getItems: cache miss for tag " + tag);
            }
        }
        //disk
        Observable<List<ItemBean>> localItems = mItemsLocalDataSource.getItems(tag, page);
        //remote
        Observable<List<ItemBean>> remoteItems = getAndSaveRemoteItems(tag, page);

        //TODO
//        if (mCacheIsDirty) {
//            return remoteItems;
//        }

        boolean isNetworkAvailable = Utils.isNetworkAvailable(GankIOApplication.getAppContext());

        if (!isNetworkAvailable) {
            Log.d(TAG, "getItems: offline. just return local data");
            return localItems;
        }

        if (page == 1) {
            Log.d(TAG, "getItems: merge Observable");
            //两路并行，本地和服务器数据回来后都会向上汇报
            return Observable.merge(localItems, remoteItems);
        } else {
            Log.d(TAG, "getItems: concat Observable");
            return Observable.concat(localItems, remoteItems)
                    //这里使用takeFirst代替First
                    .takeFirst(new Func1<List<ItemBean>, Boolean>() {
                        @Override
                        public Boolean call(List<ItemBean> beanList) {
                            Log.d(TAG, "call: beanList size = " + beanList.size());
                            //如果数据库查询够一页数据，则不用去服务器拉数据
                            return beanList.size() == AppConfig.NETWORK_DATA_PAGE_COUNT;
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
                        Log.d(TAG, "call: save remote data to local");
                        mItemsLocalDataSource.saveItems(beanList);
                        return beanList;
                    }
                }).doOnCompleted(new Action0() {
                    @Override
                    public void call() {
                        mCacheIsDirty = false;
                    }
                });
    }

    @Override
    public void saveItems(@NonNull List<ItemBean> items) {

    }

    @Override
    public void refreshItems() {
        mCacheIsDirty = true;
    }

    public void setup() {
        //根据底层配置，预拉取数据
        final String[] tags = TAB_TAG;
        for (final String tag : tags) {
            getItems(tag, 1).subscribe(new Subscriber<List<ItemBean>>() {
                @Override
                public void onCompleted() {

                }

                @Override
                public void onError(Throwable e) {

                }

                @DebugLog
                @Override
                public void onNext(List<ItemBean> beanList) {
                    mFirstPageItems.put(tag, beanList);
                }
            });
        }
    }

    public Observable<List<ItemBean>> getItems(String type, int page, boolean fromLocalOnly) {
        if (fromLocalOnly)
            return mItemsLocalDataSource.getItems(type, page);
        return getItems(type, page);
    }
}
