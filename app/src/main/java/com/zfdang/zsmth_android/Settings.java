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
    private boolean mAutoLogin;
    public boolean isAutoLogin() {
        return mAutoLogin;
    }
    public void setAutoLogin(boolean mAutoLogin) {
        if(this.mAutoLogin != mAutoLogin) {
            this.mAutoLogin = mAutoLogin;
            mEditor.putBoolean(AUTO_LOGIN, this.mAutoLogin);
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


    }
}
