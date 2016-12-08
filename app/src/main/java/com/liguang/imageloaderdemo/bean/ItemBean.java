package com.liguang.imageloaderdemo.bean;

import java.util.Arrays;

public class ItemBean {
    public String _id;
    public String createdAt;
    public String desc;
    public String[] images;
    public String publishedAt;
    public String source;
    public String type;
    public String url;
    public boolean used;
    public String who;

    public ItemBean() {

    }

    @Override
    public String toString() {
        return "ItemBean{" +
                "_id='" + _id + '\'' +
                ", createdAt='" + createdAt + '\'' +
                ", desc='" + desc + '\'' +
                ", images=" + Arrays.toString(images) +
                ", publishedAt='" + publishedAt + '\'' +
                ", source='" + source + '\'' +
                ", type='" + type + '\'' +
                ", url='" + url + '\'' +
                ", used=" + used +
                ", who='" + who + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this)
            return true;

        if (obj instanceof ItemBean) {
            ItemBean other = (ItemBean) obj;
            return other._id.equals(this._id);
        }

        return false;
    }
}
