package com.zfdang;

import android.app.Application;
import android.content.Context;

import com.zfdang.zsmth_android.helpers.GEODatabase;
import com.zfdang.zsmth_android.newsmth.UserStatus;

/**
 * Created by zfdang on 2016-3-18.
 */
public class SMTHApplication extends Application {
    // http://blog.csdn.net/lieren666/article/details/7598288
    // Android Application的作用
    private static Context context;
    public static String App_Title_Prefix = "zSMTH - ";

    public static final String FROM_BOARD = "From_Board";
    public static final String FROM_BOARD_HOT = "FROM_HOTTOPICS";
    public static final String FROM_BOARD_BOARD = "FROM_BOARDTOPICS";
    public static final String ATTACHMENT_URLS = "ATTACHMENT_URLS";
    public static final String ATTACHMENT_CURRENT_POS = "ATTACHMENT_CURRENT_POS";
    public static final String QUERY_USER_INFO = "QUERY_USER_ID";
    public static final String BOARD_OBJECT = "BOARD_OBJECT";
    public static final String TOPIC_OBJECT = "TOPIC_OBJECT";

    public static final String USER_SERVICE_RECEIVER = "USER_SERVICE_RECEIVER";

    public static final String COMPOSE_POST_CONTEXT = "Compose_Post_Context";


    // IP database
    public static GEODatabase geoDB;

    // current logined user
    public static UserStatus activeUser;

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
