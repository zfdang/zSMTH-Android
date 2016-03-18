package com.zfdang;

import android.app.Application;
import android.content.Context;

/**
 * Created by zfdang on 2016-3-18.
 */
public class zSMTHApplication extends Application {
    private static Context context;

    public void onCreate() {
        super.onCreate();
        zSMTHApplication.context = getApplicationContext();
    }

    public static Context getAppContext() {
        return zSMTHApplication.context;
    }
}
