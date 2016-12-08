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
import android.database.Cursor;
import android.support.annotation.NonNull;
import android.util.Log;

import com.liguang.imageloaderdemo.bean.ItemBean;
import com.liguang.imageloaderdemo.config.AppConfig;
import com.liguang.imageloaderdemo.data.ItemsDataSourceRxJava;
import com.liguang.imageloaderdemo.db.GankIoContract;
import com.liguang.imageloaderdemo.util.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

import static com.facebook.common.internal.Preconditions.checkNotNull;

/**
 * Concrete implementation of a data source as a db.
 */
public class ItemsLocalDataSourceRxJava implements ItemsDataSourceRxJava {
    private static final String TAG = "ItemsLocalDataSourceRxJ";

    private static ItemsLocalDataSourceRxJava INSTANCE;

    private ContentResolver mContentResolver;

    // Prevent direct instantiation.
    private ItemsLocalDataSourceRxJava(@NonNull ContentResolver contentResolver) {
        checkNotNull(contentResolver);
        mContentResolver = contentResolver;
    }

    public static ItemsLocalDataSourceRxJava getInstance(@NonNull ContentResolver contentResolver) {
        if (INSTANCE == null) {
            INSTANCE = new ItemsLocalDataSourceRxJava(contentResolver);
        }
        return INSTANCE;
    }

    @Override
    public Observable<List<ItemBean>> getItems(final String tag, final int page) {
        return Observable.create(new Observable.OnSubscribe<List<ItemBean>>() {
            @Override
            public void call(Subscriber<? super List<ItemBean>> subscriber) {
                Log.d(TAG, "call: enter read database");
                Cursor cursor =
                        mContentResolver.query(
                                GankIoContract.Item.buildItemUri(tag, AppConfig.NETWORK_DATA_PAGE_COUNT, page),
                                null, null, null, GankIoContract.Item.PUBLISHEDAT + " desc ");
                subscriber.onNext(cursor2List(cursor));
                subscriber.onCompleted();
            }
        }).subscribeOn(Schedulers.io());
    }

    private List<ItemBean> cursor2List(Cursor cursor) {
        List<ItemBean> results = new ArrayList<>();
        if (cursor == null || !cursor.moveToFirst()) {
            return results;
        }

        try {
            do {
                ItemBean bean = new ItemBean();
                bean._id = cursor
                        .getString(cursor
                                .getColumnIndex(GankIoContract.Item.SERVER_ID));
                bean.createdAt = cursor
                        .getString(cursor
                                .getColumnIndex(GankIoContract.Item.CREATEDAT));
                bean.desc = cursor
                        .getString(cursor
                                .getColumnIndex(GankIoContract.Item.DESC));
                bean.images = Utils.string2Array(cursor.getString(cursor
                        .getColumnIndex(GankIoContract.Item.IMAGES)));
                bean.publishedAt = cursor.getString(cursor
                        .getColumnIndex(GankIoContract.Item.PUBLISHEDAT));
                bean.source = cursor
                        .getString(cursor
                                .getColumnIndex(GankIoContract.Item.SOURCE));
                bean.type = cursor
                        .getString(cursor
                                .getColumnIndex(GankIoContract.Item.TYPE));
                bean.url = cursor
                        .getString(cursor
                                .getColumnIndex(GankIoContract.Item.URL));
                bean.used = cursor
                        .getInt(cursor
                                .getColumnIndex(GankIoContract.Item.USED)) == 1;
                bean.who = cursor
                        .getString(cursor
                                .getColumnIndex(GankIoContract.Item.WHO));
                results.add(bean);
            } while (cursor.moveToNext());
        } finally {
            cursor.close();
        }

        return results;
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
