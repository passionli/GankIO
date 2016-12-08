package com.liguang.imageloaderdemo.album;

public class UserInfo {
    public String url;
    public String name;

    public UserInfo(String url, String name) {
        this.url = url;
        this.name = name;
    }

    @Override
    public String toString() {
        return "UserInfo{" +
                "url='" + url + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
