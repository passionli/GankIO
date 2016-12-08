package com.liguang.imageloaderdemo.ui;

import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.util.Log;

import com.liguang.imageloaderdemo.bean.ItemBean;
import com.liguang.imageloaderdemo.config.AppConfig;
import com.liguang.imageloaderdemo.data.ItemsRepository;
import com.liguang.imageloaderdemo.data.ItemsRepositoryRxJava;
import com.liguang.imageloaderdemo.db.GankIoContract;
import com.liguang.imageloaderdemo.util.Utils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class ItemsPresenterRxJava implements ItemsContract.Presenter,
        ItemsRepository.GetItemsCallback, LoaderManager.LoaderCallbacks<Cursor> {
    private static final String TAG = "ItemsPresenterRxJava";
    Context mContext;
    //high level
    ItemsContract.View mView;
    //low level
    ItemsRepositoryRxJava mRepository;
    LoaderManager mLoaderManager;
    private String mType;
    private static final int ITEMS_LOADER = 1;
    private int mPage = 1;

    public ItemsPresenterRxJava() {

    }

    public ItemsPresenterRxJava(Context context, ItemsContract.View view, ItemsRepositoryRxJava repository, LoaderManager loaderManager, String type) {
        mContext = context;
        mView = view;
        mRepository = repository;
        mLoaderManager = loaderManager;
        mType = type;
        mView.setPresenter(this);
    }

    @Override
    public void start() {
        Log.d(TAG, "start: ");
        loadItems();
    }

    public void loadItems() {
        mView.showLoading(true);
        //下层负责创建Observable
        Observable<List<ItemBean>> observable = mRepository.getItems(mType, mPage);
        observable
                .flatMap(new Func1<List<ItemBean>, Observable<ItemBean>>() {
                    @Override
                    public Observable<ItemBean> call(List<ItemBean> beanList) {
                        return Observable.from(beanList);
                    }
                })
                .map(new Func1<ItemBean, ItemBean>() {
                    @Override
                    public ItemBean call(ItemBean itemBean) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                        try {
                            itemBean.publishedAt = sdf.format(sdf.parse(itemBean.publishedAt));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        return itemBean;
                    }
                })
                .toList()
                .subscribeOn(Schedulers.computation())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<ItemBean>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted: type = " + mType + " page = " + mPage);
                        mView.showLoading(false);
                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(List<ItemBean> beanList) {
                        //这里应该会有两步：1. 数据库 2.网络
                        Log.d(TAG, "onNext: ");
                        mView.showItems(beanList);
                    }
                });
    }

    @Override
    public void onItemsLoaded(List<ItemBean> items) {
        //这里不需要关心输入，只是收到下层(远端数据源)的回调消息,真正的数据从本地数据库拿
        if (mLoaderManager.getLoader(ITEMS_LOADER) == null) {
//            Bundle args = new Bundle();
//            args.putInt("page", 2);
            mLoaderManager.initLoader(ITEMS_LOADER, null, this);
        } else {
            mLoaderManager.restartLoader(ITEMS_LOADER, null, this);
        }
    }

    @Override
    public void onDataNotAvailable() {

    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        Log.d(TAG, "onCreateLoader: ");
        return new CursorLoader(mContext,
                GankIoContract.Item.buildItemUri(mType, AppConfig.NETWORK_DATA_PAGE_COUNT, mPage),
                null, null, null, GankIoContract.Item.PUBLISHEDAT + " desc ");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        Log.d(TAG, "onLoadFinished: ");
        if (data != null) {
            if (data.moveToLast()) {
                onDataLoaded(data);
            } else {
                onDataEmpty();
            }
        } else {
            onDataNotAvailable();
        }
    }

    private void onDataLoaded(Cursor cursor) {
        List<ItemBean> results = new ArrayList<>(cursor.getCount());
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        cursor.moveToFirst();
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
            results.add(bean);
        } while (cursor.moveToNext());


        if (mView != null) {
            mView.showLoading(false);
            mView.showItems(results);
        }
    }

    private void onDataEmpty() {

    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        onDataReset();
    }

    private void onDataReset() {

    }
}
