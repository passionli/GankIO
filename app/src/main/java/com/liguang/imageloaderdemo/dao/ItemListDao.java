package com.liguang.imageloaderdemo.dao;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.os.AsyncTask;

import com.alibaba.fastjson.JSON;
import com.liguang.imageloaderdemo.MyApplication;
import com.liguang.imageloaderdemo.bean.ItemBean;
import com.liguang.imageloaderdemo.bean.ItemListBean;
import com.liguang.imageloaderdemo.config.AppConfig;
import com.liguang.imageloaderdemo.db.GankIoContract;
import com.liguang.imageloaderdemo.network.HttpManager;
import com.liguang.imageloaderdemo.network.URLHelper;
import com.liguang.imageloaderdemo.util.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;

public class ItemListDao {

    private String tag;
    private int page;

    public ItemListDao(String tag, int page) {
        this.tag = tag;
        this.page = page;
    }

    public ItemListBean getItems() {
        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {
                String jsonData = HttpManager.getInstance().doGet(URLHelper.PREFIX +
                        tag + URLHelper.SEPERATOR + AppConfig.NETWORK_DATA_PAGE_COUNT +
                        URLHelper.SEPERATOR + page);
                ItemListBean itemListBean = JSON.parseObject(jsonData, ItemListBean.class);
                if (itemListBean != null) {
                    onDataReceive(itemListBean);
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);

        //First pull data from local
        ContentResolver resolver = MyApplication.getAppContext()
                .getContentResolver();
        Cursor cursor = resolver.query(GankIoContract.Item.buildItemUri(tag, AppConfig.NETWORK_DATA_PAGE_COUNT, page), null, null, null,
                GankIoContract.Item.PUBLISHEDAT + " desc ");
        try {
            if (cursor != null && cursor.moveToFirst()) {
                ItemListBean itemListBean = new ItemListBean();
                itemListBean.results = new ArrayList<>(cursor.getCount());
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
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

                    //为UI做处理
                    try {
                        bean.publishedAt = sdf.format(sdf.parse(bean.publishedAt));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

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
                    itemListBean.results.add(bean);
                } while (cursor.moveToNext());
                return itemListBean;
            }
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                cursor.close();
                cursor = null;
            }
        }
        return null;
    }

    private void onDataReceive(ItemListBean itemListBean) {
        int size = itemListBean.results.size();
        ContentValues[] valuesArray = new ContentValues[size];
        for (int i = 0; i < size; i++) {
            ItemBean bean = itemListBean.results.get(i);
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
        ContentResolver resolver = MyApplication
                .getAppContext().getContentResolver();
        // 批量插入。性能提升多少？
        resolver.bulkInsert(GankIoContract.Item.CONTENT_URI, valuesArray);
    }
}
