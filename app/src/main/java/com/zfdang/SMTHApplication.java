package com.zfdang;

import android.app.Application;
import android.content.Context;

import com.zfdang.zsmth_android.helpers.GEODatabase;

/**
 * Created by zfdang on 2016-3-18.
 */
public class SMTHApplication extends Application {
    // http://blog.csdn.net/lieren666/article/details/7598288
    // Android Application的作用
    private static Context context;
    public static String App_Title_Prefix = "zSMTH - ";

    // IP database
    public static GEODatabase geoDB;

    public void onCreate() {
        super.onCreate();
        SMTHApplication.context = getApplicationContext();

        // init IP lookup database
        geoDB = new GEODatabase(this);

    }

    public static Context getAppContext() {
        return SMTHApplication.context;
    }
}
