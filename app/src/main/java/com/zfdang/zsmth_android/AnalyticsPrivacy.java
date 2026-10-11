package com.zfdang.zsmth_android;

import android.content.Context;
import android.content.SharedPreferences;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;

/** Starts optional analytics only after the user explicitly opts in. */
public final class AnalyticsPrivacy {
  private static boolean initialized;
  private static boolean disabledForProcess;

  private AnalyticsPrivacy() {}

  private static SharedPreferences preferences(Context context) {
    return context.getSharedPreferences("analytics_privacy", Context.MODE_PRIVATE);
  }

  public static boolean hasChoice(Context context) {
    return preferences(context).contains("enabled");
  }

  public static boolean isEnabled(Context context) {
    return preferences(context).getBoolean("enabled", false);
  }

  public static void saveChoice(Context context, boolean enabled) {
    preferences(context).edit().putBoolean("enabled", enabled).commit();
    if (enabled) {
      initialize(context);
    } else if (initialized) {
      UMConfigure.submitPolicyGrantResult(context.getApplicationContext(), false);
      MobclickAgent.disable();
      initialized = false;
      disabledForProcess = true;
    }
  }

  public static synchronized void initialize(Context context) {
    if (initialized || disabledForProcess || !isEnabled(context)) return;
    Context appContext = context.getApplicationContext();
    UMConfigure.enableImeiCollection(false);
    UMConfigure.enableImsiCollection(false);
    UMConfigure.enableIccidCollection(false);
    UMConfigure.enableWiFiMacCollection(false);
    UMConfigure.enableAplCollection(false);
    UMConfigure.enableInstallReferrerCollection(false);
    UMConfigure.setLogEnabled(false);
    UMConfigure.preInit(appContext, "56e8c05567e58e0a9e0011cc", "UMENG_CHANNEL");
    UMConfigure.submitPolicyGrantResult(appContext, true);
    MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.MANUAL);
    UMConfigure.init(appContext, "56e8c05567e58e0a9e0011cc", "UMENG_CHANNEL",
        UMConfigure.DEVICE_TYPE_PHONE, null);
    initialized = true;
  }

  public static void onResume(Context context) {
    if (initialized && isEnabled(context)) MobclickAgent.onResume(context);
  }

  public static void onPause(Context context) {
    if (initialized && isEnabled(context)) MobclickAgent.onPause(context);
  }
}
