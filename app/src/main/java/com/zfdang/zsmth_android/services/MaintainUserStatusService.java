package com.zfdang.zsmth_android.services;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;

import com.zfdang.SMTHApplication;

import java.util.Calendar;
import java.util.GregorianCalendar;

/**
 * check login status, if not logined, login automatically if possible
 * Created by zfdang on 2016-4-4.
 */
public class MaintainUserStatusService extends IntentService {
    public static final int REQUEST_CODE = 245;

    private static final String Service_Name = "MaintainUserStatusService";
    private static final String TAG = "UserStatusService";

    public MaintainUserStatusService() {
        super(Service_Name);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // If a Context object is needed, call getApplicationContext() here.
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        // This describes what will happen when service is triggered

        ResultReceiver receiver = intent.getParcelableExtra(SMTHApplication.USER_SERVICE_RECEIVER);
        if(receiver != null) {
            Bundle bundle = new Bundle();
            bundle.putString("resultValue", "My Result Value. Passed in: ");
            // Here we call send passing a resultCode and the bundle of extras
            receiver.send(Activity.RESULT_OK, bundle);
        }
    }


    // this service can be scheduled as periodical service, call the following two methods to achieve this
    public static void schedule(Context context, UserStatusReceiver receiver) {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(context, MaintainUserStatusService.class);
        intent.putExtra(SMTHApplication.USER_SERVICE_RECEIVER, receiver);

        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getService(context, MaintainUserStatusService.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        // trigger the first alarm in 5 seconds
        Calendar c = new GregorianCalendar();
        c.add(Calendar.SECOND, 5);
        long firstMillis = c.getTimeInMillis();

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // run it immediate, and repeat in 3 minutes
        alarm.setInexactRepeating(AlarmManager.RTC_WAKEUP, firstMillis, AlarmManager.INTERVAL_FIFTEEN_MINUTES / 15, pIntent);
    }

    public static void unschedule(Context context) {
        Intent intent = new Intent(context, MaintainUserStatusService.class);
        final PendingIntent pIntent = PendingIntent.getService(context, MaintainUserStatusService.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }


}
