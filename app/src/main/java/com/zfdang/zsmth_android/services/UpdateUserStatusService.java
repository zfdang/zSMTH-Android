package com.zfdang.zsmth_android.services;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.zfdang.zsmth_android.MainActivity;
import com.zfdang.zsmth_android.R;

public class UpdateUserStatusService extends Service {

    private final String TAG = this.getClass().getSimpleName();
    private static final int ONGOING_NOTIFICATION_ID =  108;
    private boolean running = true;

    @Override
    public void onCreate() {
        super.onCreate();

        String NOTIFICATION_CHANNEL_ID = "com.zfdang.zsmth.userstatus";

        try {
            Intent notificationIntent = new Intent(this, MainActivity.class);
            PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

            Notification.Builder mBuilder = new Notification.Builder(this)
                    .setOngoing(true)
                    .setSmallIcon(R.drawable.ic_launcher)
                    .setContentTitle("zSMTH后台检查服务")
                    .setContentText("后台程序会在程序运行期间，持续检查各种通知并保持登录状态...")
                    .setContentIntent(pendingIntent);

            NotificationManager mNotifyMgr = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                NotificationChannel channel = new NotificationChannel(
                        NOTIFICATION_CHANNEL_ID,
                        "zSMTH后台检查服务",
                        NotificationManager.IMPORTANCE_LOW
                );
                mNotifyMgr.createNotificationChannel(channel);
                mBuilder.setChannelId(NOTIFICATION_CHANNEL_ID); //必须添加（Android 8.0） 【唯一标识】
            }

            Notification notification = mBuilder.build();
            startForeground(ONGOING_NOTIFICATION_ID, notification);
        } catch (Exception se) {
            Log.e(TAG, "showNotification: " + se.toString());
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand");

        // If we get killed, after returning from here, restart
        return START_STICKY;
    }


    @Override
    public void onDestroy() {
        running = false;
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
