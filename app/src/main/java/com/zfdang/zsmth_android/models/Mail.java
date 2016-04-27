package com.zfdang.zsmth_android.models;

/**
 * Created by zfdang on 2016-3-15.
 */
public  class Mail {
    public String url;
    public String title;
    public String author;
    public String date;
    public boolean isNew;

    public boolean isCategory;
    public String category;

    public Mail(String categoryName) {
        isCategory = true;
        category = categoryName;
    }

    public Mail() {
        isCategory = false;
    }

    @Override
    public String toString() {
        return "Mail{" +
                "author='" + author + '\'' +
                ", url='" + url + '\'' +
                ", title='" + title + '\'' +
                ", date='" + date + '\'' +
                ", isNew=" + isNew +
                ", isCategory=" + isCategory +
                ", category='" + category + '\'' +
                '}';
    }
}