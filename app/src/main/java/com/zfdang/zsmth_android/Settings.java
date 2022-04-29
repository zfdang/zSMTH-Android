package com.zfdang.zsmth_android;

import android.app.Activity;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.util.Log;
import com.zfdang.SMTHApplication;
import com.zfdang.devicemodeltomarketingname.DeviceMarketingName;

/**
 * Usage:
 * String username = Settings.getInstance().getUsername();
 * Settings.getInstance().setUsername("mozilla");
 */

/*
how to add a new setting:
1. create private String setting_key
2. create private local variable
3. init the variable in initSetting()
4. implement get and set methods to access the setting
*/
public class Settings {

  private static final String USERNAME_KEY = "username";
  private String mUsername;

  public String getUsername() {
    return mUsername;
  }

  public void setUsername(String mUsername) {
    if (this.mUsername == null || !this.mUsername.equals(mUsername)) {
      this.mUsername = mUsername;
      mEditor.putString(USERNAME_KEY, this.mUsername);
      mEditor.commit();
    }
  }

  private static final String PASSWORD_KEY = "password";
  private String mPassword;

  public String getPassword() {
    return mPassword;
  }

  public void setPassword(String mPassword) {
    if (this.mPassword == null || !this.mPassword.equals(mPassword)) {
      this.mPassword = mPassword;
      mEditor.putString(PASSWORD_KEY, this.mPassword);
      mEditor.commit();
    }
  }

  // save login information
  private static final String SAVE_INFO = "save_info";
  private boolean bSaveInfo;

  public boolean isSaveInfo() {
    return bSaveInfo;
  }

  public void setSaveInfo(boolean mSaveInfo) {
    if (this.bSaveInfo != mSaveInfo) {
      this.bSaveInfo = mSaveInfo;
      mEditor.putBoolean(SAVE_INFO, this.bSaveInfo);
      mEditor.commit();
    }
  }


  // last login success
  private static final String LAST_LOGIN_SUCCESS = "last_login_success";
  private boolean bLastLoginSuccess;

  public boolean isLastLoginSuccess() {
    return bLastLoginSuccess;
  }

  public void setLastLoginSuccess(boolean bLastLoginSuccess) {
    if (this.bLastLoginSuccess != bLastLoginSuccess) {
      this.bLastLoginSuccess = bLastLoginSuccess;
      mEditor.putBoolean(LAST_LOGIN_SUCCESS, this.bLastLoginSuccess);
      mEditor.commit();
    }
  }

  // after user init login action, set online = true;
  // after user init logout action, set online = false;
  // this value will impact autoLogin behaviour of service
  private static final String USER_ONLINE = "user_online";
  private boolean bUserOnline;

  public boolean isUserOnline() {
    return bUserOnline;
  }

  public void setUserOnline(boolean bUserOnline) {
    if (this.bUserOnline != bUserOnline) {
      this.bUserOnline = bUserOnline;
      mEditor.putBoolean(USER_ONLINE, this.bUserOnline);
      mEditor.commit();
    }
  }


  // use device signature or not
  private static final String USE_DEVICE_SIGNATURE = "use_device_signature";
  private boolean bUseSignature;

  public boolean bUseSignature() {
    return bUseSignature;
  }

  public void setUseSignature(boolean bUseSignature) {
    if (this.bUseSignature != bUseSignature) {
      this.bUseSignature = bUseSignature;
      mEditor.putBoolean(USE_DEVICE_SIGNATURE, this.bUseSignature);
      mEditor.commit();
    }
  }

  // customized device signature
  private static final String DEVICE_SIGNATURE = "device_signature";
  private String mSignature;

  public String getSignature() {
    if (mSignature != null && mSignature.length() > 0) {
      return mSignature;
    } else {
      return "Android";
    }
  }

  public void setSignature(String signature) {
    if (this.mSignature == null || !this.mSignature.equals(signature)) {
      this.mSignature = signature;
      mEditor.putString(DEVICE_SIGNATURE, this.mSignature);
      mEditor.commit();
    }
  }

  // show sticky topics or not
  private static final String SHOW_STICKY_TOPIC = "show_sticky_topic";
  private boolean mShowSticky;

  public boolean isShowSticky() {
    return mShowSticky;
  }

  public void setShowSticky(boolean mShowSticky) {
    if (this.mShowSticky != mShowSticky) {
      this.mShowSticky = mShowSticky;
      mEditor.putBoolean(SHOW_STICKY_TOPIC, this.mShowSticky);
      mEditor.commit();
    }
  }

  public void toggleShowSticky() {
    this.mShowSticky = !this.mShowSticky;
    mEditor.putBoolean(SHOW_STICKY_TOPIC, this.mShowSticky);
    mEditor.commit();
  }

  // save forward target
  private static final String FORWARD_TAEGET = "forward_target";
  private String mTarget;

  public String getTarget() {
    return mTarget;
  }

  public void setTarget(String target) {
    if (this.mTarget == null || !this.mTarget.equals(target)) {
      this.mTarget = target;
      mEditor.putString(FORWARD_TAEGET, this.mTarget);
      mEditor.commit();
    }
  }

  // load original image in post list, or load resized image
  // FS image viewer will always load original image
  private static final String LOAD_ORIGINAL_IMAGE = "LOAD_ORIGINAL_IMAGE";
  private boolean bLoadOriginalImage;

  public boolean isLoadOriginalImage() {
    return bLoadOriginalImage;
  }

  public void setLoadOriginalImage(boolean bLoadOriginalImage) {
    if (this.bLoadOriginalImage != bLoadOriginalImage) {
      this.bLoadOriginalImage = bLoadOriginalImage;
      mEditor.putBoolean(LOAD_ORIGINAL_IMAGE, this.bLoadOriginalImage);
      mEditor.commit();
    }
  }

  // use normal login, or with verification
  private static final String LOGIN_WITH_VERIFICATION = "LOGIN_WITH_VERIFICATION";
  private boolean bLoginWithVerification;

  public boolean isLoginWithVerification() {
    return bLoginWithVerification;
  }

  public void setLoginWithVerification(boolean value) {
    if (this.bLoginWithVerification != value) {
      this.bLoginWithVerification = value;
      mEditor.putBoolean(LOGIN_WITH_VERIFICATION, this.bLoginWithVerification);
      mEditor.commit();
    }
  }

  // load image from cdn, or from smth website directly
  // https://www.mysmth.net/nForum/#!article/PocketLife/3100239
  // https://static.mysmth.net/nForum/#!article/PocketLife/3100239
  private static final String IMAGE_SOURCE_CDN = "IMAGE_SOURCE_CDN";
  private boolean bImageSourceCDN;

  public boolean isImageSourceCDN() {
    return bImageSourceCDN;
  }

  public void setImageSourceCDN(boolean value) {
    if (this.bImageSourceCDN != value) {
      this.bImageSourceCDN = value;
      mEditor.putBoolean(IMAGE_SOURCE_CDN, this.bImageSourceCDN);
      mEditor.commit();
    }
  }

  // show board master only items in postlist actions
  private static final String BOARD_MASTER_ONLY = "BOARD_MASTER_ONLY";
  private boolean bBoardMasterOnly;

  public boolean isBoardMasterOnly() {
    return bBoardMasterOnly;
  }

  public void setBoardMasterOnly(boolean bBoardMasterOnly) {
    if (this.bBoardMasterOnly != bBoardMasterOnly) {
      this.bBoardMasterOnly = bBoardMasterOnly;
      mEditor.putBoolean(BOARD_MASTER_ONLY, this.bBoardMasterOnly);
      mEditor.commit();
    }
  }

  // night mode
  private static final String NIGHT_MODE = "NIGHT_MODE";
  private boolean bNightMode;

  public boolean isNightMode() {
    return bNightMode;
  }

  public void setNightMode(boolean bNightMode) {
    if (this.bNightMode != bNightMode) {
      this.bNightMode = bNightMode;
      mEditor.putBoolean(NIGHT_MODE, this.bNightMode);
      mEditor.commit();
    }
  }

  private static final String LAST_LAUNCH_VERSION = "LAST_LAUNCH_VERSION";
  private int iLastVersion;

  public boolean isFirstRun() {
    PackageManager pm = SMTHApplication.getAppContext().getPackageManager();
    int currentVersion = 0;
    try {
      PackageInfo pi = pm.getPackageInfo(SMTHApplication.getAppContext().getPackageName(), 0);
      currentVersion = pi.versionCode;
    } catch (PackageManager.NameNotFoundException e) {
      Log.e("Setting", "isFirstRun: " + Log.getStackTraceString(e));
    }

    if (currentVersion == iLastVersion) {
      return false;
    } else {
      this.iLastVersion = currentVersion;
      mEditor.putInt(LAST_LAUNCH_VERSION, this.iLastVersion);
      mEditor.commit();
      return true;
    }
  }

  private static final String NOTIFICATION_MAIL = "NOTIFICATION_MAIL";
  private boolean bNotificationMail;

  public boolean isNotificationMail() {
    return bNotificationMail;
  }

  public void setNotificationMail(boolean bNotificationMail) {
    if (this.bNotificationMail != bNotificationMail) {
      this.bNotificationMail = bNotificationMail;
      mEditor.putBoolean(NOTIFICATION_MAIL, this.bNotificationMail);
      mEditor.commit();
    }
  }

  private static final String NOTIFICATION_AT = "NOTIFICATION_AT";
  private boolean bNotificationAt;

  public boolean isNotificationAt() {
    return bNotificationAt;
  }

  public void setNotificationAt(boolean bNotificationAt) {
    if (this.bNotificationAt != bNotificationAt) {
      this.bNotificationAt = bNotificationAt;
      mEditor.putBoolean(NOTIFICATION_AT, this.bNotificationAt);
      mEditor.commit();
    }
  }

  private static final String NOTIFICATION_LIKE = "NOTIFICATION_LIKE";
  private boolean bNotificationLike;

  public boolean isNotificationLike() {
    return bNotificationLike;
  }

  public void setNotificationLike(boolean bNotificationLike) {
    if (this.bNotificationLike != bNotificationLike) {
      this.bNotificationLike = bNotificationLike;
      mEditor.putBoolean(NOTIFICATION_LIKE, this.bNotificationLike);
      mEditor.commit();
    }
  }

  private static final String NOTIFICATION_REPLY = "NOTIFICATION_REPLY";
  private boolean bNotificationReply;

  public boolean isNotificationReply() {
    return bNotificationReply;
  }

  public void setNotificationReply(boolean bNotificationReply) {
    if (this.bNotificationReply != bNotificationReply) {
      this.bNotificationReply = bNotificationReply;
      mEditor.putBoolean(NOTIFICATION_REPLY, this.bNotificationReply);
      mEditor.commit();
    }
  }

  private static final String LAUNCH_HOTTOPIC_AS_ENTRY = "LAUNCH_HOTTOPIC_AS_ENTRY";
  private boolean bLaunchHotTopic;

  public boolean isLaunchHotTopic() {
    return bLaunchHotTopic;
  }

  public void setLaunchHotTopic(boolean bLaunchHotTopic) {
    if (this.bLaunchHotTopic != bLaunchHotTopic) {
      this.bLaunchHotTopic = bLaunchHotTopic;
      mEditor.putBoolean(LAUNCH_HOTTOPIC_AS_ENTRY, this.bLaunchHotTopic);
      mEditor.commit();
    }
  }

  private static final String SHOW_POST_NAVITATION_BAR = "SHOW_POST_NAVITATION_BAR";
  private boolean bPostNavBar;

  public boolean hasPostNavBar() {
    return bPostNavBar;
  }

  public void setPostNavBar(boolean bPostNavBar) {
    if (this.bPostNavBar != bPostNavBar) {
      this.bPostNavBar = bPostNavBar;
      mEditor.putBoolean(SHOW_POST_NAVITATION_BAR, this.bPostNavBar);
      mEditor.commit();
    }
  }

  private static final String VOLUME_KEY_SCROLL = "VOLUME_KEY_SCROLL";
  private boolean bVolumeKeyScroll;

  public boolean isVolumeKeyScroll() {
    return bVolumeKeyScroll;
  }

  public void setVolumeKeyScroll(boolean bVolumeKeyScroll) {
    if (this.bVolumeKeyScroll != bVolumeKeyScroll) {
      this.bVolumeKeyScroll = bVolumeKeyScroll;
      mEditor.putBoolean(VOLUME_KEY_SCROLL, this.bVolumeKeyScroll);
      mEditor.commit();
    }
  }


  // defined in arrays.xml
  // 0: large font; 1: normal font; 2: small, 3: extra small 4: extra large 5: extremely large
  // defaut: 1 - normal font
  private static final String ZSMTH_FONT_INDEX = "ZSMTH_FONT_INDEX";
  private int iFontIndex;

  public int getFontIndex() {
    return iFontIndex;
  }

  public float getFontSizeFloatValue() {
    if (iFontIndex == 1) {
      return 1.0f;
    } else if (iFontIndex == 2) {
      return 0.85f;
    } else if (iFontIndex == 0) {
      return 1.15f;
    } else if (iFontIndex == 3) {
      return 0.65f;
    } else if (iFontIndex == 4) {
      return 1.35f;
    } else if (iFontIndex == 5) {
      return 1.55f;
    }
    return 1.0f;
  }

  public void setFontIndex(int iFontIndex) {
    System.out.println(iFontIndex);
    if (this.iFontIndex != iFontIndex) {
      this.iFontIndex = iFontIndex;
      mEditor.putInt(ZSMTH_FONT_INDEX, this.iFontIndex);
      mEditor.commit();
    }
  }

  // we will cache contents in compose post activity, so that user can restore contents
  private static final String COMPOSE_POST_CACHE = "COMPOSE_POST_CACHE";
  private String mPostCache;

  public String getPostCache() {
    return mPostCache;
  }

  public void setPostCache(String mPostCache) {
    if (this.mPostCache == null || !this.mPostCache.equals(mPostCache)) {
      this.mPostCache = mPostCache;
      mEditor.putString(COMPOSE_POST_CACHE, this.mPostCache);
      mEditor.commit();
    }
  }

  private final String Preference_Name = "ZSMTH_Config";

  private SharedPreferences mPreference;
  private SharedPreferences.Editor mEditor;

  // Singleton
  private static Settings ourInstance = new Settings();

  public static Settings getInstance() {
    return ourInstance;
  }

  private Settings() {
    initSettings();
  }

  // load all settings from SharedPreference
  private void initSettings() {
    // this
    mPreference = SMTHApplication.getAppContext().getSharedPreferences(Preference_Name, Activity.MODE_PRIVATE);
    mEditor = mPreference.edit();

    // load all values from preference to variables
    mShowSticky = mPreference.getBoolean(SHOW_STICKY_TOPIC, false);
    mUsername = mPreference.getString(USERNAME_KEY, "");
    mPassword = mPreference.getString(PASSWORD_KEY, "");
    bSaveInfo = mPreference.getBoolean(SAVE_INFO, true);
    bLastLoginSuccess = mPreference.getBoolean(LAST_LOGIN_SUCCESS, false);
    bUserOnline = mPreference.getBoolean(USER_ONLINE, false);

    bUseSignature = mPreference.getBoolean(USE_DEVICE_SIGNATURE, true);
    mSignature = mPreference.getString(DEVICE_SIGNATURE, "");
    if (mSignature.length() == 0) {
      String marketingName = DeviceMarketingName.getInstance(SMTHApplication.getAppContext()).getDeviceMarketingName(true);
      setSignature(marketingName);
    }

    mTarget = mPreference.getString(FORWARD_TAEGET, "");

    bLoadOriginalImage = mPreference.getBoolean(LOAD_ORIGINAL_IMAGE, true);
    bLoginWithVerification = mPreference.getBoolean(LOGIN_WITH_VERIFICATION, true);
    bImageSourceCDN = mPreference.getBoolean(IMAGE_SOURCE_CDN, true);

    bBoardMasterOnly = mPreference.getBoolean(BOARD_MASTER_ONLY, false);

    bNightMode = mPreference.getBoolean(NIGHT_MODE, true);

    iLastVersion = mPreference.getInt(LAST_LAUNCH_VERSION, 0);

    bNotificationMail = mPreference.getBoolean(NOTIFICATION_MAIL, true);
    bNotificationAt = mPreference.getBoolean(NOTIFICATION_AT, true);
    bNotificationLike = mPreference.getBoolean(NOTIFICATION_LIKE, true);
    bNotificationReply = mPreference.getBoolean(NOTIFICATION_REPLY, true);

    bLaunchHotTopic = mPreference.getBoolean(LAUNCH_HOTTOPIC_AS_ENTRY, true);

    bPostNavBar = mPreference.getBoolean(SHOW_POST_NAVITATION_BAR, true);
    bVolumeKeyScroll = mPreference.getBoolean(VOLUME_KEY_SCROLL, true);
    iFontIndex = mPreference.getInt(ZSMTH_FONT_INDEX, 1);

    mPostCache = mPreference.getString(COMPOSE_POST_CACHE, "");
  }
}
