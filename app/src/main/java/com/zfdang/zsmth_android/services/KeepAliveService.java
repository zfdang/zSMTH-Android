package com.zfdang.zsmth_android.services;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.R;
import com.zfdang.zsmth_android.newsmth.SMTHHelper;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

public class KeepAliveService extends Service {
    private static final int notId = 1;
    private static final String channelId = "e729";
    private final static int interval = 300; //  刷新的时间间隔，单位：秒
    private final static int retry = 60; //  刷新失败后重试的时间间隔，单位：秒
    private static int i = 0;
    private static int count = 0;           //  网络访问出错的次数

    @SuppressLint("NotificationId0")
    @Override
    public void onCreate() {
        super.onCreate();
        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("保持在线服务")
                .setContentText("在线中……")
                .setSmallIcon(R.drawable.ic_launcher)
                .setPriority(NotificationCompat.PRIORITY_MIN)
                .setChannelId(channelId)
                .build();
        notification.flags |= Notification.FLAG_NO_CLEAR;

        startForeground(notId, notification);
        OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(MyWork.class)
                .setInitialDelay(interval, TimeUnit.SECONDS)
                .build();
        WorkManager.getInstance(getApplicationContext()).enqueue(workRequest);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    public static class MyWork extends Worker {
        public MyWork(Context context, WorkerParameters workerParams) {
            super(context, workerParams);
        }

        @NonNull
        @Override
        public Result doWork() {
            ++i;
            Log.d("KeepAliveService", "保持用户在线服务计数: " + i);

            try {
                //  因为这是在service线程中的网络访问，所以同步调用并不会影响UI
                //  也无需关心返回值，只要与水木服务器通信，即可达到保持登录状态的目的
                SMTHHelper.getInstance().wService.keepAlive().execute();
                count = 0;
                doNext(interval);
            } catch (IOException e) {
                ++count;
                //  事不过三，超过三次与水木的通信失败，就停止服务
                //  因为这时的登录状态大概率已经过期，继续刷新已无意义
                Log.d("KeepAliveService", "与服务器通讯失败计数: " + count);

                if (count > 3) {
                    Log.d("KeepAliveService", "STOP");
                    SMTHApplication.mUserStatusReceiver.onServiceFailed();
                } else {
                    Log.d("KeepAliveService", "retry: " + count);
                    doNext(retry);
                }
            }
            return Result.success();
        }

        void doNext(int sec) {
            OneTimeWorkRequest workRequest = new OneTimeWorkRequest.Builder(MyWork.class)
                    .setInitialDelay(sec, TimeUnit.SECONDS)
                    .build();
            WorkManager.getInstance(getApplicationContext()).enqueue(workRequest);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        throw new UnsupportedOperationException("Not yet implemented");
    }

    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    "SMTH", NotificationManager.IMPORTANCE_DEFAULT);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            channel.setSound(null, null);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.d("KeepAliveService", "onDestroy");
        stopForeground(true);
    }
    public KeepAliveService() {
    }
}