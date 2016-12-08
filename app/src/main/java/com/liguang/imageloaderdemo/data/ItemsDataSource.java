
package com.liguang.imageloaderdemo.data;

import android.support.annotation.NonNull;

import com.liguang.imageloaderdemo.bean.ItemBean;

import java.util.List;

public interface ItemsDataSource {

    interface GetItemsCallback {

        void onItemsLoaded(List<ItemBean> items);

        void onDataNotAvailable();
    }

    void getItems(String tag, int page, @NonNull GetItemsCallback callback);

    void saveItems(@NonNull List<ItemBean> items);

}