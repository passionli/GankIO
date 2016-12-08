package com.liguang.imageloaderdemo.bean;

import java.util.List;

public class ItemListBean {
    public boolean error;
    public List<ItemBean> results;

    public ItemListBean() {

    }

    @Override
    public String toString() {
        return "ItemListBean{" +
                "error=" + error +
                ", results=" + results +
                '}';
    }
}
