/*
 * Copyright 2016, The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.liguang.imageloaderdemo.data.local;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.support.annotation.NonNull;

import com.liguang.imageloaderdemo.bean.ItemBean;
import com.liguang.imageloaderdemo.data.ItemsDataSource;
import com.liguang.imageloaderdemo.db.GankIoContract;

import java.util.Arrays;
import java.util.List;

import static com.facebook.common.internal.Preconditions.checkNotNull;

/**
 * Concrete implementation of a data source as a db.
 */
public class ItemsLocalDataSource implements ItemsDataSource {

    private static ItemsLocalDataSource INSTANCE;

    private ContentResolver mContentResolver;

    // Prevent direct instantiation.
    private ItemsLocalDataSource(@NonNull ContentResolver contentResolver) {
        checkNotNull(contentResolver);
        mContentResolver = contentResolver;
    }

    public static ItemsLocalDataSource getInstance(@NonNull ContentResolver contentResolver) {
        if (INSTANCE == null) {
            INSTANCE = new ItemsLocalDataSource(contentResolver);
        }
        return INSTANCE;
    }

    @Override
    public void getItems(String tag, int page, @NonNull GetItemsCallback callback) {

    }

    @Override
    public void saveItems(@NonNull List<ItemBean> items) {
        checkNotNull(items);

        int size = items.size();
        ContentValues[] valuesArray = new ContentValues[size];
        for (int i = 0; i < size; i++) {
            ItemBean bean = items.get(i);
            ContentValues values = new ContentValues();
            values.put(GankIoContract.Item.SERVER_ID, bean._id);
            values.put(GankIoContract.Item.CREATEDAT, bean.createdAt);
            values.put(GankIoContract.Item.DESC, bean.desc);
            values.put(GankIoContract.Item.IMAGES, Arrays.toString(bean.images));
            values.put(GankIoContract.Item.PUBLISHEDAT, bean.publishedAt);
            values.put(GankIoContract.Item.SOURCE, bean.source);
            values.put(GankIoContract.Item.TYPE, bean.type);
            values.put(GankIoContract.Item.URL, bean.url);
            values.put(GankIoContract.Item.USED, bean.used);
            values.put(GankIoContract.Item.WHO, bean.who);
            valuesArray[i] = values;
        }
        mContentResolver.bulkInsert(GankIoContract.Item.CONTENT_URI, valuesArray);
    }
}
