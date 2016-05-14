package com.zfdang.zsmth_android.helpers;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by zfdang on 2016-5-14.
 */
public class ActivityUtils {
    public static void openLink(String link, Activity activity) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
        activity.startActivity(browserIntent);
    }


    public static void sendEmail(String link, Activity activity) {
        /* Create the Intent */
        final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

        /* Fill it with Data */
        emailIntent.setType("plain/text");
        emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[]{link});
        emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "来自zSMTH的邮件");
        emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, "");

        /* Send it off to the Activity-Chooser */
        activity.startActivity(Intent.createChooser(emailIntent, "发邮件..."));
    }

}
