package com.zfdang;

import android.app.Application;
import android.content.Context;
import androidx.appcompat.app.AppCompatDelegate;
import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.backends.okhttp3.OkHttpImagePipelineConfigFactory;
import com.facebook.imagepipeline.core.ImagePipelineConfig;
import com.mob.MobSDK;
import com.umeng.commonsdk.UMConfigure;
import com.zfdang.zsmth_android.Settings;
import com.zfdang.zsmth_android.helpers.GEODatabase;
import com.zfdang.zsmth_android.newsmth.SMTHHelper;
import com.zfdang.zsmth_android.newsmth.UserStatus;
import com.zfdang.zsmth_android.services.UserStatusReceiver;

import okhttp3.OkHttpClient;
import androidx.multidex.MultiDex;

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

  public static final String MAIL_OBJECT = "MAIL_OBJECT";

  // MaintainUserStatusService to UserStatusReceiver, to onNewIntent
  public static final String SERVICE_NOTIFICATION_MESSAGE = "SERVICE_NOTIFICATION_MESSAGE";

  public static final String USER_SERVICE_RECEIVER = "USER_SERVICE_RECEIVER";

  public static final String COMPOSE_POST_CONTEXT = "Compose_Post_Context";

  public static final String NOTIFICATION_NEW_MAIL = "你有新邮件!";
  public static final String NOTIFICATION_NEW_AT = "你有新@!";
  public static final String NOTIFICATION_NEW_REPLY = "你有新回复!";
  public static final String NOTIFICATION_NEW_LIKE = "你有新Like!";
  public static final String NOTIFICATION_LOGIN_LOST = "登录已过期！请重新登录...";

  public static final int INTERVAL_TO_CHECK_MESSAGE = 2; // 2 minutes for interval to check messages
  public static UserStatusReceiver mUserStatusReceiver = null;

  // IP database
  public static GEODatabase geoDB;

  @Override
  protected void attachBaseContext(Context base) {
    super.attachBaseContext(base);
    MultiDex.install(this);
  }

  // current logined user
  public static UserStatus activeUser;
  public static String displayedUserId = "guest";
  public static boolean isValidUser() {
    return activeUser != null && !activeUser.getId().equals("guest");
  }


  public void onCreate() {
    super.onCreate();
    SMTHApplication.context = getApplicationContext();

    // init IP lookup database
    geoDB = new GEODatabase(this);

    // init shareSDK
    MobSDK.init(this);

    // init umeng SDK
    UMConfigure.init(this, "56e8c05567e58e0a9e0011cc", "UMENG_CHANNEL", UMConfigure.DEVICE_TYPE_PHONE, null);

    // init Fresco
    // Set<RequestListener> requestListeners = new HashSet<>();
    // requestListeners.add(new RequestLoggingListener());
    OkHttpClient httpClient = SMTHHelper.getInstance().mHttpClient;
    ImagePipelineConfig config = OkHttpImagePipelineConfigFactory.newBuilder(context, httpClient)
        //                .setRequestListeners(requestListeners)
                        .setDownsampleEnabled(false)
        .build();
    Fresco.initialize(context, config);
    // FLog.setMinimumLoggingLevel(FLog.VERBOSE);

    boolean bNightMode = Settings.getInstance().isNightMode();
    if (bNightMode) {
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
    } else {
      AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
    }
  }

  public static Context getAppContext() {
    return SMTHApplication.context;
  }
}
