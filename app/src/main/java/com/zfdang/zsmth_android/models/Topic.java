package com.zfdang.zsmth_android.models;

/**
 * Created by zfdang on 2016-3-14.
 */
/**
 * A dummy item representing a piece of content.
 */
public class Topic {
    public final String id;
    public final String content;
    public final String details;

    public Topic(String id, String content, String details) {
        this.id = id;
        this.content = content;
        this.details = details;
    }

    @Override
    public String toString() {
        return content;
    }
}