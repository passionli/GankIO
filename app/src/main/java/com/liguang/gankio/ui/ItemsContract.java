package com.liguang.gankio.ui;

import com.liguang.gankio.bean.ItemBean;
import com.liguang.gankio.framework.BasePresenter;
import com.liguang.gankio.framework.BaseView;

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
        void reloadItemsFromLocal();
    }
}
