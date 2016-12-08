package com.liguang.imageloaderdemo.ui;

import com.liguang.imageloaderdemo.bean.ItemBean;
import com.liguang.imageloaderdemo.framework.BasePresenter;
import com.liguang.imageloaderdemo.framework.BaseView;

import java.util.List;

public class ItemsContract {
    interface View extends BaseView<Presenter> {

        void showLoading(boolean display);

        void showItems(List<ItemBean> beanList);

        void showNoItems();

        void showRecyclerView(boolean display);
    }

    interface Presenter extends BasePresenter {
        void loadItems(boolean forceUpdate);
    }
}
