package com.liguang.imageloaderdemo.album.model;

import java.io.Serializable;

public class FileItem implements Serializable {
    public boolean isPhoto;
    public String uri;
    public String text;

    public FileItem(String uri, String text) {
        this.uri = uri;
        this.text = text;
    }
}
