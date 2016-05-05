package com.zfdang.zsmth_android;

import android.app.Activity;
import android.content.SharedPreferences;

import com.zfdang.SMTHApplication;



/**
 * Usage:
 *   String username = Settings.getInstance().getUsername();
 *   Settings.getInstance().setUsername("mozilla");
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
        if(this.mUsername == null || !this.mUsername.equals(mUsername)){
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
        if(this.mPassword == null || !this.mPassword.equals(mPassword)){
            this.mPassword = mPassword;
            mEditor.putString(PASSWORD_KEY, this.mPassword);
            mEditor.commit();
        }
    }


    private static final String AUTO_LOGIN = "auto_login";
    private boolean bAutoLogin;
    public boolean isAutoLogin() {
        return bAutoLogin;
    }
    public void setAutoLogin(boolean mAutoLogin) {
        if(this.bAutoLogin != mAutoLogin) {
            this.bAutoLogin = mAutoLogin;
            mEditor.putBoolean(AUTO_LOGIN, this.bAutoLogin);
            mEditor.commit();
        }
    }


    private static final String LAST_LOGIN_SUCCESS = "last_login_success";
    private boolean bLastLoginSuccess;
    public boolean isLastLoginSuccess() {
        return bLastLoginSuccess;
    }
    public void setLastLoginSuccess(boolean bLastLoginSuccess) {
        if(this.bLastLoginSuccess != bLastLoginSuccess) {
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
        if(this.bUserOnline != bUserOnline) {
            this.bUserOnline = bUserOnline;
            mEditor.putBoolean(USER_ONLINE, this.bUserOnline);
            mEditor.commit();
        }
    }


    private static final String DEVICE_SIGNATURE = "device_signature";
    private String mSignature;
    public String getSignature() {
        if(mSignature!= null && mSignature.length() > 0) {
            return mSignature;
        } else {
            return "Android";
        }
    }
    public void setSignature(String signature) {
        if(this.mSignature == null || !this.mSignature.equals(signature)){
            this.mSignature = signature;
            mEditor.putString(DEVICE_SIGNATURE, this.mSignature);
            mEditor.commit();
        }
    }


    private static final String SHOW_STICKY_TOPIC = "show_sticky_topic";
    private boolean mShowSticky;
    public boolean isShowSticky() {
        return mShowSticky;
    }
    public void setShowSticky(boolean mShowSticky) {
        if(this.mShowSticky != mShowSticky) {
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


    private static final String FORWARD_TAEGET = "forward_target";
    private String mTarget;
    public String getTarget() {
        return mTarget;
    }
    public void setTarget(String target) {
        if(this.mTarget == null || !this.mTarget.equals(target)){
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
        if(this.bLoadOriginalImage != bLoadOriginalImage) {
            this.bLoadOriginalImage = bLoadOriginalImage;
            mEditor.putBoolean(LOAD_ORIGINAL_IMAGE, this.bLoadOriginalImage);
            mEditor.commit();
        }
    }


    private static final String LAST_LAUNCH_VERSION = "last_launch_version";

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
    private void initSettings(){
        // this
        mPreference = SMTHApplication.getAppContext().getSharedPreferences(Preference_Name, Activity.MODE_PRIVATE);
        mEditor = mPreference.edit();

        // load all values from preference to variables
        mShowSticky = mPreference.getBoolean(SHOW_STICKY_TOPIC, false);
        mUsername = mPreference.getString(USERNAME_KEY, "");
        mPassword = mPreference.getString(PASSWORD_KEY, "");
        bAutoLogin = mPreference.getBoolean(AUTO_LOGIN, true);

        bLastLoginSuccess = mPreference.getBoolean(LAST_LOGIN_SUCCESS, false);

        mSignature = mPreference.getString(DEVICE_SIGNATURE, "Android");
        mTarget = mPreference.getString(FORWARD_TAEGET, "");

        bUserOnline = mPreference.getBoolean(USER_ONLINE, false);

        bLoadOriginalImage = mPreference.getBoolean(LOAD_ORIGINAL_IMAGE, false);
    }
}
