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

import android.os.AsyncTask;
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

/**
 * Implementation of the data source as network.
 */
public class ItemsRemoteDataSource implements ItemsDataSource {
    private static final String TAG = "ItemsRemoteDataSource";
    private static ItemsRemoteDataSource INSTANCE;

    private ItemsRemoteDataSource() {

    }

    @Override
    public void getItems(final String tag, final int page, @NonNull final GetItemsCallback callback) {
        new AsyncTask<Void, Void, List<ItemBean>>() {
            @Override
            protected List<ItemBean> doInBackground(Void... params) {
                int retry = 5;
                int i = 0;
//                for (; i < retry; i++) {
                Log.i(TAG, "getItems: i = " + i);
                String jsonData = HttpManager.getInstance().doGet(URLHelper.PREFIX +
                        tag + URLHelper.SEPERATOR + AppConfig.NETWORK_DATA_PAGE_COUNT +
                        URLHelper.SEPERATOR + page);
                ItemListBean itemListBean = JSON.parseObject(jsonData, ItemListBean.class);
                if (itemListBean != null && !itemListBean.error) {
                    return itemListBean.results;
                }
//                }
                return null;
            }

            @Override
            protected void onPostExecute(List<ItemBean> beanList) {
                if (beanList != null) {
                    callback.onItemsLoaded(beanList);
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
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