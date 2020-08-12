package com.zfdang.zsmth_android.services;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.JobIntentService;
import android.text.TextUtils;
import android.util.Log;

import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.Settings;
import com.zfdang.zsmth_android.newsmth.SMTHHelper;
import com.zfdang.zsmth_android.newsmth.UserInfo;
import com.zfdang.zsmth_android.newsmth.UserStatus;

import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * check login status, if not logined, login automatically if possible
 * Created by zfdang on 2016-4-4.
 */
public class MaintainUserStatusService extends JobIntentService {
    private static final int JOB_ID = 1483252;
    private static final String TAG = "UserStatusService"; //Limit to 23 characters for logging

    private static UserStatusReceiver mUserStatusReceiver = null;

    public static void enqueueWork(Context context, Intent work, UserStatusReceiver receiver) {
        mUserStatusReceiver = receiver;
        enqueueWork(context, MaintainUserStatusService.class, JOB_ID, work);
    }

    public String getNotificationMessage(UserStatus userStatus) {
        String message = "";
        Settings settings = Settings.getInstance();
        if (userStatus.isNew_mail() && settings.isNotificationMail()) {
            message = SMTHApplication.NOTIFICATION_NEW_MAIL + "  ";
        }
        if (userStatus.getNew_like() > 0 && settings.isNotificationLike()) {
            message += SMTHApplication.NOTIFICATION_NEW_LIKE + "  ";
        }
        if (userStatus.getNew_at() > 0 && settings.isNotificationAt()) {
            message += SMTHApplication.NOTIFICATION_NEW_AT + "  ";
        }
        if (userStatus.getNew_reply() > 0 && settings.isNotificationReply()) {
            message += SMTHApplication.NOTIFICATION_NEW_REPLY + "  ";
        }
        //Log.d(TAG, message);
        return message;
    }

    @Override
    protected void onHandleWork(@android.support.annotation.NonNull Intent intent) {
        // This describes what will happen when service is triggered
        final SMTHHelper helper = SMTHHelper.getInstance();
//        Log.d(TAG, "onHandleWork");
        helper.wService.queryActiveUserStatus().observeOn(Schedulers.io()).subscribeOn(Schedulers.io()).subscribe(
                new Observer<UserStatus>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable disposable) {
                    }

                    @Override
                    public void onNext(@NonNull UserStatus userStatus) {
//                        Log.d(TAG, "1.0 User Status: " + userStatus.getId());
//                        if (SMTHApplication.activeUser != null) {
//                            Log.d(TAG, "1.0 Cached User: " + SMTHApplication.activeUser.getId());
//                        } else {
//                            Log.d(TAG, "1.0 Cached Status: null ");
//                        }

                        // 4 possibilities:
                        // 1.1 valid user, but not cached
                        // 1.2 valid user, and cached
                        // 1.3 invalid user, but not cached
                        // 1.4 invalid user, but cached.
                        String userid = userStatus.getId();
                        int caseID = 0;
                        if (userid != null && !TextUtils.equals(userid, "guest")) {
                            // valid logined user
                            if (SMTHApplication.activeUser == null || !TextUtils.equals(userid, SMTHApplication.activeUser.getId())) {
                                // case 1.1
                                // update face_url for userStatus, and cache userStatus
                                UserInfo userInfo = helper.wService.queryUserInformation(userid).blockingFirst();
                                if (userInfo != null) {
//                                    Log.d(TAG, "1.1 Update User Info: " + userInfo.toString());
                                    userStatus.setFace_url(userInfo.getFace_url());
                                    SMTHApplication.activeUser = userStatus;
                                }
                                caseID = 1;
                            } else {
                                // case 1.2
//                                Log.d(TAG, "1.2 New user is the same with cached user");
                                // do nothing
//                              userStatus.setFace_url(SMTHApplication.activeUser.getFace_url());
                                caseID = 2;
                            }
                        } else {
                            // invalid login user
                            if (SMTHApplication.activeUser == null) {
                                // case 1.3
                                // do nothing
//                                Log.d(TAG, "1.3 invalid user, and not cached");
                                caseID = 3;
                                SMTHApplication.activeUser = userStatus;  //Save "guest" case for activeuser initialization
                            } else {
                                // case 1.4, clear login information
                                SMTHApplication.activeUser = null;
//                                Log.d(TAG, "1.4 invalid user, but cached");
                                caseID = 4;
                            }
                        }

                        // 2. now to update application status
//                        Log.d(TAG, "2.0 case id is " + caseID);

                        // get message when valid logined user
                        String message = "";
                        if (caseID == 1 || caseID == 2) {
                            message = getNotificationMessage(userStatus);
                        }
//                        Log.d(TAG, "2.1 notification message is " + message);

                        // send update notification: 1. caseID =1 or caseID = 4 2. new message
                        if ((caseID == 1 || caseID == 4 || message.length() > 0) && mUserStatusReceiver != null) {
//                            Log.d(TAG, "2.2 send notification");
                            Bundle bundle = new Bundle();
                            if (message.length() > 0) {
                                bundle.putString(SMTHApplication.SERVICE_NOTIFICATION_MESSAGE, message);
                            }
                            // Here we call send passing a resultCode and the bundle of extras
                            mUserStatusReceiver.send(Activity.RESULT_OK, bundle);
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Log.e(TAG, "onError: " + Log.getStackTraceString(e));
                    }

                    @Override
                    public void onComplete() {

                    }
                }); // .subscribe

        // stop the service
        stopSelf();
    }
}
