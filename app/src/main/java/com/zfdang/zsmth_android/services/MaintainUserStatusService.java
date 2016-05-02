package com.zfdang.zsmth_android.services;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.text.TextUtils;
import android.util.Log;

import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.Settings;
import com.zfdang.zsmth_android.newsmth.AjaxResponse;
import com.zfdang.zsmth_android.newsmth.SMTHHelper;
import com.zfdang.zsmth_android.newsmth.UserInfo;
import com.zfdang.zsmth_android.newsmth.UserStatus;

import java.util.List;

import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

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

    public static void schedule(Context context, UserStatusReceiver receiver) {
        // Construct an intent that will execute the AlarmReceiver
        Intent intent = new Intent(context, MaintainUserStatusService.class);
        intent.putExtra(SMTHApplication.USER_SERVICE_RECEIVER, receiver);

        // Create a PendingIntent to be triggered when the alarm goes off
        final PendingIntent pIntent = PendingIntent.getService(context, MaintainUserStatusService.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        //repeat in 3 minutes
        alarm.setRepeating(AlarmManager.RTC, System.currentTimeMillis(), AlarmManager.INTERVAL_FIFTEEN_MINUTES / 10, pIntent);
    }

    public static void unschedule(Context context) {
        Intent intent = new Intent(context, MaintainUserStatusService.class);
        final PendingIntent pIntent = PendingIntent.getService(context, MaintainUserStatusService.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        alarm.cancel(pIntent);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        // If a Context object is needed, call getApplicationContext() here.
    }


    // this service can be scheduled as periodical service, call the following two methods to achieve this

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(final Intent intent) {
        // This describes what will happen when service is triggered
        // process here:
        // 1. get user status
        // 2.1 if it's not guest, go to step 3
        // 2.2 if it's guest, login;
        // 2.2.1 if login success, get user status (2.2.1.1) again. go to step 3
        // 2.2.2 if login failed, go to step 3
        // 3. check whether user status == SMTHApplication.activeUser
        // 3.1 if they are same, just return SMTHApplication.activeUser
        // 3.2 if not, get real face URL
        // 4. if user status is a different user, send notification to receiver to update navigationView

        final SMTHHelper helper = SMTHHelper.getInstance();
        Log.d(TAG, "onHandleIntent: 1. to get current UserStatus from remote");
        helper.wService.queryActiveUserStatus()
                .map(new Func1<UserStatus, UserStatus>() {
                    // check it's logined user, or guest
                    @Override
                    public UserStatus call(UserStatus userStatus) {
                        if (userStatus != null && userStatus.getId() != null && !userStatus.getId().equals("guest")) {
                            // logined user, just return the status for next step
                            Log.d(TAG, "call: 2.1 valid logined user: " + userStatus.getId());
                            return userStatus;
                        }

                        // login first
                        Log.d(TAG, "call: " + "2.2 user not logined, try to login now..");
                        final Settings setting = Settings.getInstance();
                        String username = setting.getUsername();
                        String password = setting.getPassword();
                        boolean bAutoLogin = setting.isAutoLogin();
                        boolean bLastSuccess = setting.isLastLoginSuccess();
                        boolean bUserOnline = setting.isUserOnline();
                        boolean bLoginSuccess = false;
                        Log.d(TAG, "call: " + String.format("Autologin: %b, LastSuccess: %b, Online: %b", bAutoLogin, bLastSuccess, bUserOnline));
                        if (bAutoLogin && bLastSuccess && bUserOnline) {
                            List<Integer> results = helper.wService.login(username, password, "7")
                                    .map(new Func1<AjaxResponse, Integer>() {
                                        @Override
                                        public Integer call(AjaxResponse response) { // 参数类型 String
                                            if(response.getAjax_st() == 1){
                                                // {"ajax_st":1,"ajax_code":"0005","ajax_msg":"操作成功"}
                                                return SMTHHelper.AJAX_RESULT_OK;
                                            } else if(response.getAjax_code().equals("0005")) {
                                                // {"ajax_st":0,"ajax_code":"0101","ajax_msg":"您的用户名并不存在，或者您的密码错误"}
                                                return SMTHHelper.AJAX_RESULT_FAILED;

                                            }
                                            return SMTHHelper.AJAX_RESULT_UNKNOWN;
                                        }
                                    })
                                    .toList().toBlocking().single();

                            if (results != null && results.size() == 1) {
                                int result = results.get(0);
                                if (result == SMTHHelper.AJAX_RESULT_OK) {
                                    // set flag, so that we will query user status again
                                    Log.d(TAG, "call: 2.2.1. Login success");
                                    bLoginSuccess = true;
                                } else if (result == SMTHHelper.AJAX_RESULT_FAILED) {
                                    // set flag, so that we will not login again next time
                                    Log.d(TAG, "call: 2.2.2. Login failed");
                                    setting.setLastLoginSuccess(false);
                                }
                            }

                        } // if (bAutoLogin && bLastSuccess)

                        // try to find new UserStatus only when login success
                        if (bLoginSuccess) {
                            Log.d(TAG, "call: " + "2.2.1.1 try to get userstatus again after login action");
                            List<UserStatus> stats = helper.queryActiveUserStatus().toList().toBlocking().single();
                            if (stats != null && stats.size() == 1) {
                                return stats.get(0);
                            }
                            return null;
                        } else {
                            return userStatus;
                        }
                    }
                })
                .map(new Func1<UserStatus, UserStatus>() {
                         // try to find user's real faceurl
                         @Override
                         public UserStatus call(UserStatus userStatus) {
                             String userid = userStatus.getId();
                             if(userid != null && SMTHApplication.activeUser != null && userid.equals(SMTHApplication.activeUser.getId())) {
                                 // current user is already cached in SMTHApplication
                                 Log.d(TAG, "call: " + "3.1 New user is the same with cached user, copy faceURL from local");
                                 userStatus.setFace_url(SMTHApplication.activeUser.getFace_url());
                                 return userStatus;
                             }

                             if (userid != null && !userid.equals("guest")) {
                                 // get correct faceURL
                                 Log.d(TAG, "call: " + "3.2 New user is different, get real face URL from remote");
                                 List<UserInfo> users = helper.wService.queryUserInformation(userid).toList().toBlocking().single();
                                 if (users.size() == 1) {
                                     UserInfo user = users.get(0);
                                     userStatus.setFace_url(user.getFace_url());
                                 }
                             }
                             return userStatus;
                         }
                     }
                )
                .observeOn(Schedulers.io())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<UserStatus>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: " + Log.getStackTraceString(e));
                    }

                    @Override
                    public void onNext(UserStatus userStatus) {
                        if (userStatus == null || userStatus.getId() == null) return;

                        Log.d(TAG, "onNext: " + userStatus.toString());

                        String userid = userStatus.getId();
                        if(SMTHApplication.activeUser != null && TextUtils.equals(SMTHApplication.activeUser.getId(), userid) && userStatus.isNew_mail()) {
                            // the same user, but with new mail coming
                            Log.d(TAG, "onNext: " + "4.1 same user with new mail, send notification");
                            ResultReceiver receiver = intent.getParcelableExtra(SMTHApplication.USER_SERVICE_RECEIVER);
                            if (receiver != null) {
                                Bundle bundle = new Bundle();
                                bundle.putString(SMTHApplication.SERVICE_NOTIFICATION_MESSAGE, "你有新邮件!");
                                // Here we call send passing a resultCode and the bundle of extras
                                receiver.send(Activity.RESULT_OK, bundle);
                            }

                        } else if (SMTHApplication.activeUser == null || !TextUtils.equals(SMTHApplication.activeUser.getId(), userid)) {
                            // different user or new user
                            Log.d(TAG, "onNext: " + "4.2 different user, send notification: ");
                            SMTHApplication.activeUser = userStatus;

                            // send  notification to receiver
                            ResultReceiver receiver = intent.getParcelableExtra(SMTHApplication.USER_SERVICE_RECEIVER);
                            if (receiver != null) {
                                Bundle bundle = new Bundle();
                                if(userStatus.isNew_mail()) {
                                    // set new mail flag here
                                    bundle.putString(SMTHApplication.SERVICE_NOTIFICATION_MESSAGE, "你有新邮件!");
                                }
                                // Here we call send passing a resultCode and the bundle of extras
                                receiver.send(Activity.RESULT_OK, bundle);
                            }
                        } else {
                            Log.d(TAG, "onNext: " + "4.3 Same user without new mail, skip notification!");
                        }
                    }
                } // new Subscriber<UserStatus>()
                ); // .subscribe
    }


}
