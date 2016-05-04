package com.zfdang.zsmth_android;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.github.machinarius.preferencefragment.PreferenceFragment;
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
public class MyPreferenceFragment extends PreferenceFragment {
    private static final String TAG = "PreferenceFragment";
    Preference fresco_cache;
    Preference okhttp3_cache;
    Preference signature_control;
    Preference signature_content;
    Preference image_control;
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

        signature_control = findPreference("setting_signature_control");

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

        image_control = findPreference("setting_image_control");

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
                        Log.e(TAG, "onError: " + Log.getStackTraceString(e));
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
        view.setBackgroundColor(getResources().getColor(R.color.colorPrimaryLight));

        return view;
    }

    @Override
    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

}
