package com.zfdang.devicemodeltomarketingname;

import android.content.Context;
import android.os.Build;
import android.util.Log;

import java.io.InputStream;
import java.util.Properties;

/**
 * Created by zfdang on 2016-5-9.
 */
public class DeviceMarketingName {
    private Context mContext;
    private final String MarketingName_Properties_Filename = "marketingName.properties";
    private final String Branding_Properties_Filename = "branding.properties";

    private static final String TAG = "DeviceMarketingName";

    private Properties brandingProp = null;
    private Properties marketingNameProp = null;


    private static DeviceMarketingName ourInstance = null;

    public static DeviceMarketingName getInstance(Context context) {
        if(ourInstance == null) {
            ourInstance = new DeviceMarketingName(context);
        }
        return ourInstance;
    }

    private DeviceMarketingName(Context context) {
        mContext = context;
    }

    public String getDeviceMarketingName(boolean withBranding) {
        if (marketingNameProp == null) {
            marketingNameProp = new Properties();
            try {
                InputStream fis = mContext.getAssets().open(MarketingName_Properties_Filename);
                marketingNameProp.load(fis);
                fis.close();
            } catch (Exception e) {
                Log.e(TAG, "getDeviceMarketingName: " + Log.getStackTraceString(e));
            }
        }

//        marketingNameProp.list(System.out);

        if (withBranding && brandingProp == null) {
            brandingProp = new Properties();
            try {
                InputStream fis = mContext.getAssets().open(Branding_Properties_Filename);
                brandingProp.load(fis);
                fis.close();
            } catch (Exception e) {
                Log.e(TAG, "getDeviceMarketingName: " + Log.getStackTraceString(e));
            }
        }

        String model = Build.MODEL;
        String marketingName = marketingNameProp.getProperty(model, model);
        if (withBranding) {
            String branding = brandingProp.getProperty(model, "");
            if (branding.length() > 0) {
                return String.format("%s-%s", branding, marketingName);
            }
        }
        return marketingName;
    }


}
