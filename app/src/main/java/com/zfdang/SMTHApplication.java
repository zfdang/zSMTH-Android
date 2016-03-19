package com.zfdang;

import android.app.Application;
import android.content.Context;

/**
 * Created by zfdang on 2016-3-18.
 */
public class SMTHApplication extends Application {
    // http://blog.csdn.net/lieren666/article/details/7598288
    // Android Application的作用
    private static Context context;

    public void onCreate() {
        super.onCreate();
        SMTHApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return SMTHApplication.context;
    }
}
