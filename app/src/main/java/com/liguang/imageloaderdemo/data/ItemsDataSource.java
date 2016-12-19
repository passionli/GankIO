
package com.liguang.imageloaderdemo.data;

import android.support.annotation.NonNull;

import com.liguang.imageloaderdemo.bean.ItemBean;

import java.util.List;

import rx.Observable;

public interface ItemsDataSource {

    Observable<List<ItemBean>> getItems(String tag, int page);

    void saveItems(@NonNull List<ItemBean> items);

    void refreshItems();
}