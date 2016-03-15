package com.zfdang.zsmth_android.models;

/**
 * Created by zfdang on 2016-3-15.
 */
public  class Mail {
    public final String id;
    public final String content;
    public final String details;

    public Mail(String id, String content, String details) {
        this.id = id;
        this.content = content;
        this.details = details;
    }

    @Override
    public String toString() {
        return content;
    }
}