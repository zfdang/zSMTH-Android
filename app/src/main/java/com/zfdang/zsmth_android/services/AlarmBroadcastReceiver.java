package com.zfdang.zsmth_android.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.SystemClock;
import android.util.Log;

public class AlarmBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = "AlarmBroadcastReceiver";
    public static final int REQUEST_CODE = 245;

    private static UserStatusReceiver mUserStatusReceiver = null;
    private static Context mContext = null;
    private static AlarmManager alarmManager;
    private static PendingIntent pendingIntent;

    @Override
    public void onReceive(Context context, Intent intent) {
//        Log.d(TAG, "onReceive, run background job now");
        Intent nIntent = new Intent(mContext, MaintainUserStatusService.class);
        MaintainUserStatusService.enqueueWork(mContext, nIntent, mUserStatusReceiver);
    }

    // this service can be scheduled as periodical service, call the following two methods to achieve this
    public static void schedule(Context context, UserStatusReceiver receiver) {
        mUserStatusReceiver = receiver;
        mContext = context;

        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(context, AlarmBroadcastReceiver.class);
        // Create a PendingIntent to be triggered when the alarm goes off
        pendingIntent = PendingIntent.getBroadcast(context, REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        // first triggered in 3 seconds, repeated every 1 minute
        alarmManager.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 3000, 60000, pendingIntent);
    }

    public static void unschedule() {
        if (alarmManager != null) {
            alarmManager.cancel(pendingIntent);
        }
    }

    public static void runJobNow(Context context, UserStatusReceiver receiver) {
        Intent nIntent = new Intent(context, MaintainUserStatusService.class);
        MaintainUserStatusService.enqueueWork(context, nIntent, receiver);
    }
}
