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

package com.liguang.imageloaderdemo.data.remote;

import android.support.annotation.NonNull;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.liguang.imageloaderdemo.bean.ItemBean;
import com.liguang.imageloaderdemo.bean.ItemListBean;
import com.liguang.imageloaderdemo.config.AppConfig;
import com.liguang.imageloaderdemo.data.ItemsDataSource;
import com.liguang.imageloaderdemo.network.HttpManager;
import com.liguang.imageloaderdemo.network.URLHelper;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Implementation of the data source as network.
 */
public class ItemsRemoteDataSource implements ItemsDataSource {
    private static final String TAG = "ItemsRemoteDataSource";
    private static ItemsDataSource INSTANCE;

    private ItemsRemoteDataSource() {

    }

    @Override
    public Observable<List<ItemBean>> getItems(final String tag, final int page) {
        return Observable.create(new Observable.OnSubscribe<List<ItemBean>>() {
            @Override
            public void call(Subscriber<? super List<ItemBean>> subscriber) {
                Log.d(TAG, "call: enter http level pull data");
                String jsonData = HttpManager.getInstance().doGet(URLHelper.PREFIX +
                        tag + URLHelper.SEPERATOR + AppConfig.NETWORK_DATA_PAGE_COUNT +
                        URLHelper.SEPERATOR + page);
                Log.d(TAG, "call: response : " + jsonData);
                ItemListBean itemListBean = JSON.parseObject(jsonData, ItemListBean.class);
                if (itemListBean != null && !itemListBean.error) {
                    subscriber.onNext(itemListBean.results);
                    subscriber.onCompleted();
                } else {
                    subscriber.onError(new Exception("http level error"));
                }
                Log.d(TAG, "call: exit http level pull data");
            }
        }).subscribeOn(Schedulers.io());
    }

    @Override
    public void saveItems(@NonNull List<ItemBean> items) {
        //TODO upload data to network
    }

    public static ItemsDataSource getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new ItemsRemoteDataSource();
        }
        return INSTANCE;
    }
}