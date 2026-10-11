package com.zfdang.zsmth_android.services;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.annotation.NonNull;
import androidx.work.Constraints;
import androidx.work.ExistingWorkPolicy;
import androidx.work.NetworkType;
import androidx.work.OneTimeWorkRequest;
import androidx.work.WorkManager;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.newsmth.SMTHHelper;
import java.io.IOException;
import java.util.concurrent.TimeUnit;

/** Session refresh uses scheduled network work rather than a permanent foreground service. */
public final class KeepAliveService {
    private static final String WORK_NAME = "forum-session-refresh";
    private static final String WORK_TAG = "forum-session-refresh-v2";
    private static final int INTERVAL_SECONDS = 300;
    private static final int RETRY_SECONDS = 60;

    private KeepAliveService() {}

    private static SharedPreferences preferences(Context context) {
        return context.getSharedPreferences(WORK_NAME, Context.MODE_PRIVATE);
    }

    public static void start(Context context) {
        preferences(context).edit().putBoolean("enabled", true).apply();
        enqueue(context, INTERVAL_SECONDS, ExistingWorkPolicy.KEEP);
    }

    public static void stop(Context context) {
        preferences(context).edit().putBoolean("enabled", false).putInt("failures", 0).commit();
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME);
    }

    private static void enqueue(Context context, int delay, ExistingWorkPolicy policy) {
        OneTimeWorkRequest request = new OneTimeWorkRequest.Builder(MyWork.class)
                .addTag(WORK_TAG)
                .setConstraints(new Constraints.Builder().setRequiredNetworkType(NetworkType.CONNECTED).build())
                .setInitialDelay(delay, TimeUnit.SECONDS)
                .build();
        WorkManager.getInstance(context).enqueueUniqueWork(WORK_NAME, policy, request);
    }

    // Keep the worker's class name so upgrades can safely finish queued legacy requests.
    public static class MyWork extends Worker {
        public MyWork(Context context, WorkerParameters parameters) { super(context, parameters); }

        @NonNull @Override public Result doWork() {
            Context context = getApplicationContext();
            SharedPreferences state = preferences(context);
            if (isStopped() || !getTags().contains(WORK_TAG) || !state.getBoolean("enabled", false)) {
                return Result.success();
            }
            int nextDelay = INTERVAL_SECONDS;
            try {
                if (!SMTHHelper.getInstance().wService.keepAlive().execute().isSuccessful()) {
                    throw new IOException("Session refresh failed");
                }
                state.edit().putInt("failures", 0).commit();
            } catch (IOException error) {
                int failures = state.getInt("failures", 0) + 1;
                state.edit().putInt("failures", failures).commit();
                if (failures > 3) {
                    KeepAliveService.stop(context);
                    if (SMTHApplication.mUserStatusReceiver != null) {
                        SMTHApplication.mUserStatusReceiver.onServiceFailed();
                    }
                    return Result.success();
                }
                nextDelay = RETRY_SECONDS;
            }
            if (!isStopped() && state.getBoolean("enabled", false)) {
                enqueue(context, nextDelay, ExistingWorkPolicy.APPEND_OR_REPLACE);
            }
            return Result.success();
        }
    }
}
