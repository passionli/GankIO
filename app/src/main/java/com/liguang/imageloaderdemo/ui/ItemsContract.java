package com.liguang.imageloaderdemo.ui;

import com.liguang.imageloaderdemo.bean.ItemBean;
import com.liguang.imageloaderdemo.framework.BasePresenter;
import com.liguang.imageloaderdemo.framework.BaseView;

import java.util.List;

public class ItemsContract {
    interface View extends BaseView<Presenter> {

        void showFooterLoading();
        void hideFooterLoading();

        void showHeaderRefreshing();
        void hideHeaderRefreshing();

        void showItems(List<ItemBean> beanList);

        void showNoMoreItems();

        void showError();

        void showRecyclerView();
        void hideRecyclerView();
    }

    interface Presenter extends BasePresenter {
        void loadItems(boolean forceUpdate);
    }
}
