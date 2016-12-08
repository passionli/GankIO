package com.liguang.imageloaderdemo.data;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.liguang.imageloaderdemo.bean.ItemBean;

import java.util.List;

public class ItemsRepository implements ItemsDataSource {
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

    @Override
    public void getItems(String tag, int page, @NonNull final GetItemsCallback callback) {
        //触发上层去读数据库
        callback.onItemsLoaded(null);
        //if page == 1 start remote data source
        //else fetch local data source firstly
        //if count >= 25 则使用本地数据
        //else 去服务器拉数据下来
        mItemsRemoteDataSource.getItems(tag, page, new GetItemsCallback() {
            @Override
            public void onItemsLoaded(List<ItemBean> items) {
                //pass data to parent context
                refreshLocalDataSource(items);
                callback.onItemsLoaded(null);
            }

            @Override
            public void onDataNotAvailable() {
                callback.onDataNotAvailable();
            }
        });
    }

    private void refreshLocalDataSource(List<ItemBean> items) {
        mItemsLocalDataSource.saveItems(items);
    }

    @Override
    public void saveItems(@NonNull List<ItemBean> items) {

    }

    //Callback to high level, e.g presenter level
    public interface LoadDataCallback {
        void onDataLoaded(Cursor data);

        void onDataEmpty();

        void onDataNotAvailable();

        void onDataReset();
    }
}
