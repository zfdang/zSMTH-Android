package com.zfdang.zsmth_android.models;

/**
 * Created by zfdang on 2016-3-16.
 */
public class Contributor {
    public final String login;
    public final int contributions;
    public Contributor(String login, int contributions) {
        this.login = login;
        this.contributions = contributions;
    }
    @Override
    public String toString() {
        return "Contributor{" +
                "login='" + login + '\'' +
                ", contributions=" + contributions +
                '}';
    }
}
