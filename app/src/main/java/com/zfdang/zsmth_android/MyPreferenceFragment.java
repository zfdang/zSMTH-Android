package com.zfdang.zsmth_android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.helpers.FileLess;
import com.zfdang.zsmth_android.helpers.FileSizeUtil;

import java.io.File;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by zfdang on 2016-5-2.
 */
public class MyPreferenceFragment extends PreferenceFragmentCompat {
    private static final String TAG = "PreferenceFragment";
    Preference fresco_cache;
    Preference okhttp3_cache;
    CheckBoxPreference signature_control;
    Preference signature_content;
    CheckBoxPreference image_quality_control;
    CheckBoxPreference daynight_control;

    CheckBoxPreference notification_control_mail;
    CheckBoxPreference notification_control_like;
    CheckBoxPreference notification_control_reply;
    CheckBoxPreference notification_control_at;


    Preference app_version;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        addPreferencesFromResource(R.xml.preferences);

        fresco_cache = findPreference("setting_fresco_cache");
        fresco_cache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // clear cache, then update cache size
                ImagePipeline imagePipeline = Fresco.getImagePipeline();
                imagePipeline.clearDiskCaches();

                updateFrescoCache();
                return true;
            }
        });

        okhttp3_cache = findPreference("setting_okhttp3_cache");
        okhttp3_cache.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                // clear cache, then update cache size
                File cache = new File(SMTHApplication.getAppContext().getCacheDir(), "Responses");
                FileLess.$del(cache);
                if (!cache.exists()) {
                    cache.mkdir();
                }

                updateOkHttp3Cache();
                return true;
            }
        });

        signature_control = (CheckBoxPreference) findPreference("setting_signature_control");
        // ignore signature_control at the moment

        signature_content = findPreference("setting_signature_content");
        signature_content.setSummary(Settings.getInstance().getSignature());
        if(signature_content instanceof EditTextPreference) {
            // set default value in editing dialog
            EditTextPreference et = (EditTextPreference) signature_content;
            et.setText(Settings.getInstance().getSignature());
        }
        signature_content.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                String signature = newValue.toString();
                Settings.getInstance().setSignature(signature);
                signature_content.setSummary(signature);
                return true;
            }
        });


        image_quality_control = (CheckBoxPreference) findPreference("setting_image_quality_control");
        image_quality_control.setChecked(Settings.getInstance().isLoadOriginalImage());
        image_quality_control.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean bLoadOriginalImage = Settings.getInstance().isLoadOriginalImage();
                if(newValue instanceof Boolean){
                    Boolean boolVal = (Boolean) newValue;
                    bLoadOriginalImage = boolVal;
                }
                Settings.getInstance().setLoadOriginalImage(bLoadOriginalImage);
                return true;
            }
        });


        daynight_control = (CheckBoxPreference) findPreference("setting_daynight_control");
        daynight_control.setChecked(Settings.getInstance().isNightMode());
        daynight_control.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean bNightMode = Settings.getInstance().isNightMode();
                if(newValue instanceof Boolean){
                    Boolean boolVal = (Boolean) newValue;
                    bNightMode = boolVal;
                }
                Settings.getInstance().setNightMode(bNightMode);

                setApplicationNightMode();
                return true;
            }
        });


        notification_control_mail = (CheckBoxPreference) findPreference("setting_notification_control_mail");
        notification_control_mail.setChecked(Settings.getInstance().isNotificationMail());
        notification_control_mail.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean bValue = Settings.getInstance().isNotificationMail();
                if(newValue instanceof Boolean){
                    Boolean boolVal = (Boolean) newValue;
                    bValue = boolVal;
                }
                Settings.getInstance().setNotificationMail(bValue);
                return true;
            }
        });


        notification_control_at = (CheckBoxPreference) findPreference("setting_notification_control_at");
        notification_control_at.setChecked(Settings.getInstance().isNotificationAt());
        notification_control_at.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean bValue = Settings.getInstance().isNotificationAt();
                if(newValue instanceof Boolean){
                    Boolean boolVal = (Boolean) newValue;
                    bValue = boolVal;
                }
                Settings.getInstance().setNotificationAt(bValue);
                return true;
            }
        });

        notification_control_like = (CheckBoxPreference) findPreference("setting_notification_control_like");
        notification_control_like.setChecked(Settings.getInstance().isNotificationLike());
        notification_control_like.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean bValue = Settings.getInstance().isNotificationLike();
                if(newValue instanceof Boolean){
                    Boolean boolVal = (Boolean) newValue;
                    bValue = boolVal;
                }
                Settings.getInstance().setNotificationLike(bValue);
                return true;
            }
        });


        notification_control_reply = (CheckBoxPreference) findPreference("setting_notification_control_reply");
        notification_control_reply.setChecked(Settings.getInstance().isNotificationReply());
        notification_control_reply.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean bValue = Settings.getInstance().isNotificationReply();
                if(newValue instanceof Boolean){
                    Boolean boolVal = (Boolean) newValue;
                    bValue = boolVal;
                }
                Settings.getInstance().setNotificationReply(bValue);
                return true;
            }
        });


        app_version = findPreference("setting_app_version");
        app_version.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("http://zsmth-android.zfdang.com/release.html")));
                return true;
            }
        });

        updateOkHttp3Cache();
        updateFrescoCache();
        updateVersionInfo();
    }

    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
    }

    public void setApplicationNightMode() {
        boolean bNightMode = Settings.getInstance().isNightMode();
        if(bNightMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }

        Activity activity = getActivity();
        if(activity != null) {
            activity.recreate();
        }
    }

    public void updateVersionInfo() {
        Context context = SMTHApplication.getAppContext();
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            String version = pInfo.versionName;
            int verCode = pInfo.versionCode;
            String summary = String.format("版本号: %s(%d)", version, verCode);
            app_version.setSummary(summary);
        } catch (Exception e) {
            Log.e(TAG, "updateVersionInfo: " + Log.getStackTraceString(e) );
        }
    }

    public void updateOkHttp3Cache() {
        File httpCacheDirectory = new File(SMTHApplication.getAppContext().getCacheDir(), "Responses");
        updateCacheSize(httpCacheDirectory.getAbsolutePath(), okhttp3_cache);
    }

    public void updateFrescoCache() {
        File frescoCacheDirectory = new File(SMTHApplication.getAppContext().getCacheDir(), "image_cache");
        // Log.d(TAG, "updateFrescoCache: " + frescoCacheDirectory.getAbsolutePath());
        updateCacheSize(frescoCacheDirectory.getAbsolutePath(), fresco_cache);
    }


    public void updateCacheSize(final String folder, final Preference pref) {
        Observable.just(folder)
                .map(new Func1<String, String>() {
                    @Override
                    public String call(String s) {
                        return FileSizeUtil.getAutoFileOrFolderSize(s);
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(SMTHApplication.getAppContext(), "获取缓存大小失败!\n" + e.toString(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNext(String s) {
                        Log.d(TAG, "onNext: Folder size = " + s);
                        pref.setSummary("缓存大小:" + s);
                    }
                });

    }

    @Override
    public View onCreateView(LayoutInflater paramLayoutInflater, ViewGroup paramViewGroup, Bundle paramBundle) {
        // http://stackoverflow.com/questions/16970209/preferencefragment-background-color
        View view = super.onCreateView(paramLayoutInflater, paramViewGroup, paramBundle);
        view.setBackgroundColor(getResources().getColor(R.color.preference_background));

        return view;
    }
}
