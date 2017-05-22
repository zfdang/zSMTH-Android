package com.zfdang.zsmth_android;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.AppCompatDelegate;
import android.support.v7.preference.CheckBoxPreference;
import android.support.v7.preference.EditTextPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.util.Log;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.helpers.ActivityUtils;
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

    CheckBoxPreference launch_hottopic_as_entry;
    CheckBoxPreference daynight_control;
    CheckBoxPreference setting_post_navigation_control;
    CheckBoxPreference setting_volume_key_scroll;
    ListPreference setting_fontsize_control;
    CheckBoxPreference image_quality_control;

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




        launch_hottopic_as_entry = (CheckBoxPreference) findPreference("launch_hottopic_as_entry");
        launch_hottopic_as_entry.setChecked(Settings.getInstance().isLaunchHotTopic());
        launch_hottopic_as_entry.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean bValue = Settings.getInstance().isLaunchHotTopic();
                if(newValue instanceof Boolean){
                    Boolean boolVal = (Boolean) newValue;
                    bValue = boolVal;
                }
                Settings.getInstance().setLaunchHotTopic(bValue);
                return true;
            }
        });

        setting_post_navigation_control = (CheckBoxPreference) findPreference("setting_post_navigation_control");
        setting_post_navigation_control.setChecked(Settings.getInstance().hasPostNavBar());
        setting_post_navigation_control.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean bValue = Settings.getInstance().hasPostNavBar();
                if(newValue instanceof Boolean){
                    Boolean boolVal = (Boolean) newValue;
                    bValue = boolVal;
                }
                Settings.getInstance().setPostNavBar(bValue);
                return true;
            }
        });

        setting_volume_key_scroll = (CheckBoxPreference) findPreference("setting_volume_key_scroll");
        setting_volume_key_scroll.setChecked(Settings.getInstance().isVolumeKeyScroll());
        setting_volume_key_scroll.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean bValue = Settings.getInstance().isVolumeKeyScroll();
                if(newValue instanceof Boolean){
                    Boolean boolVal = (Boolean) newValue;
                    bValue = boolVal;
                }
                Settings.getInstance().setVolumeKeyScroll(bValue);
                return true;
            }
        });


        setting_fontsize_control = (ListPreference) findPreference("setting_fontsize_control");
        setting_fontsize_control.setValueIndex(Settings.getInstance().getFontIndex());
        setting_fontsize_control.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener(){
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                int fontIndex = Settings.getInstance().getFontIndex();
                if(newValue instanceof String){
                    fontIndex = Integer.parseInt((String)newValue);
                }
                Settings.getInstance().setFontIndex(fontIndex);

                // recreate activity for font size to take effect
                Activity activity = getActivity();
                if(activity != null) {
                    activity.recreate();
                }
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



        signature_control = (CheckBoxPreference) findPreference("setting_signature_control");
        signature_control.setChecked(Settings.getInstance().bUseSignature());
        signature_control.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean bValue = Settings.getInstance().bUseSignature();
                if(newValue instanceof Boolean){
                    Boolean boolVal = (Boolean) newValue;
                    bValue = boolVal;
                }
                Settings.getInstance().setUseSignature(bValue);
                if(bValue == false) {
                    String alipay = "dantifer@gmail.com";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                        final android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager)
                                getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        final android.content.ClipData clipData = android.content.ClipData.newPlainText("ID", alipay);
                        clipboardManager.setPrimaryClip(clipData);
                    } else {
                        final android.text.ClipboardManager clipboardManager = (android.text.ClipboardManager) getActivity().getSystemService(Context.CLIPBOARD_SERVICE);
                        clipboardManager.setText(alipay);
                    }
                    Toast.makeText(getActivity(), "作者支付宝ID已复制到剪贴板...", Toast.LENGTH_SHORT).show();
                }
                return true;
            }
        });

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


        app_version = findPreference("setting_app_version");
        app_version.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                ActivityUtils.openLink("http://zsmth-android.zfdang.com/release.html", getActivity());
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
        activity.getWindow().setWindowAnimations(R.style.WindowAnimationFadeInOut);
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
}
