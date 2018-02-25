package com.zfdang.zsmth_android.services;

import android.app.Activity;
import android.app.AlarmManager;
import android.app.IntentService;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.SystemClock;
import android.text.TextUtils;
import android.util.Log;
import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.Settings;
import com.zfdang.zsmth_android.helpers.MakeList;
import com.zfdang.zsmth_android.newsmth.AjaxResponse;
import com.zfdang.zsmth_android.newsmth.SMTHHelper;
import com.zfdang.zsmth_android.newsmth.UserInfo;
import com.zfdang.zsmth_android.newsmth.UserStatus;
import io.reactivex.Observer;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import java.util.List;

/**
 * check login status, if not logined, login automatically if possible
 * Created by zfdang on 2016-4-4.
 */
public class MaintainUserStatusService extends IntentService {
  public static final int REQUEST_CODE = 245;

  private static final String Service_Name = "MaintainUserStatusService";
  private static final String TAG = "UserStatusService";
  private static UserStatusReceiver userStatusReceiver = null;

  public MaintainUserStatusService() {
    super(Service_Name);
  }

  @Override public void onCreate() {
    super.onCreate();
  }

  @Override public void onDestroy() {
    super.onDestroy();
  }

  // this service can be scheduled as periodical service, call the following two methods to achieve this
  public static void schedule(Context context, UserStatusReceiver receiver) {
    // Construct an intent that will execute the AlarmReceiver
    Intent intent = new Intent(context, MaintainUserStatusService.class);
    userStatusReceiver = receiver;

    // Create a PendingIntent to be triggered when the alarm goes off
    final PendingIntent pIntent =
        PendingIntent.getService(context, MaintainUserStatusService.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);

    AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    // first triggered in 20 second, repeated every 1 minute
    alarm.setRepeating(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 20000, AlarmManager.INTERVAL_FIFTEEN_MINUTES / 15,
        pIntent);
  }

  public static void unschedule(Context context) {
    Intent intent = new Intent(context, MaintainUserStatusService.class);
    final PendingIntent pIntent =
        PendingIntent.getService(context, MaintainUserStatusService.REQUEST_CODE, intent, PendingIntent.FLAG_UPDATE_CURRENT);
    AlarmManager alarm = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    alarm.cancel(pIntent);
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
    if (userStatus.getNew_at() && settings.isNotificationAt()) {
      message += SMTHApplication.NOTIFICATION_NEW_AT + "  ";
    }
    if (userStatus.getNew_reply() && settings.isNotificationReply()) {
      message += SMTHApplication.NOTIFICATION_NEW_REPLY + "  ";
    }
    //Log.d(TAG, message);
    return message;
  }

  @Override protected void onHandleIntent(final Intent intent) {
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
    //Log.d(TAG, "1.0 get current UserStatus from remote");
    helper.wService.queryActiveUserStatus().map(new Function<UserStatus, UserStatus>() {
      @Override public UserStatus apply(@NonNull UserStatus userStatus) throws Exception {
        //Log.d(TAG, "2.0 " + userStatus.toString());

        // check it's logined user, or guest
        if (userStatus != null && userStatus.getId() != null && !userStatus.getId().equals("guest")) {
          // logined user, just return the status for next step
          //Log.d(TAG, "call: 2.1 valid logined user: " + userStatus.getId());
          return userStatus;
        }

        // login first
        //Log.d(TAG, "call: " + "2.2 user not logined, try to login now...");
        final Settings setting = Settings.getInstance();
        String username = setting.getUsername();
        String password = setting.getPassword();
        boolean bAutoLogin = setting.isAutoLogin();
        boolean bLastSuccess = setting.isLastLoginSuccess();
        boolean bUserOnline = setting.isUserOnline();
        boolean bLoginSuccess = false;
        //Log.d(TAG, "call: 2.2.1 " + String.format("Autologin: %b, LastSuccess: %b, Online: %b", bAutoLogin, bLastSuccess, bUserOnline));
        if (bAutoLogin && bLastSuccess && bUserOnline) {
          Iterable<Integer> its = helper.wService.login(username, password, "7").map(new Function<AjaxResponse, Integer>() {
            @Override public Integer apply(@NonNull AjaxResponse response) throws Exception {
              if (response.getAjax_st() == 1) {
                // {"ajax_st":1,"ajax_code":"0005","ajax_msg":"操作成功"}
                return AjaxResponse.AJAX_RESULT_OK;
              } else if (response.getAjax_code().equals("0005")) {
                // {"ajax_st":0,"ajax_code":"0101","ajax_msg":"您的用户名并不存在，或者您的密码错误"}
                return AjaxResponse.AJAX_RESULT_FAILED;
              }
              return AjaxResponse.AJAX_RESULT_UNKNOWN;
            }
          }).blockingIterable();

          List<Integer> results = MakeList.makeList(its);

          //Log.d(TAG, "call: 2.2.2 " + results.size());
          if (results != null && results.size() == 1) {
            int result = results.get(0);
            if (result == AjaxResponse.AJAX_RESULT_OK) {
              // set flag, so that we will query user status again
              //Log.d(TAG, "call: 2.2.3. Login success");
              bLoginSuccess = true;
            } else if (result == AjaxResponse.AJAX_RESULT_FAILED) {
              // set flag, so that we will not login again next time
              //Log.d(TAG, "call: 2.2.4. Login failed");
              setting.setLastLoginSuccess(false);
            }
          }
        } // if (bAutoLogin && bLastSuccess)

        // try to find new UserStatus only when login success
        if (bLoginSuccess) {
          //Log.d(TAG, "call: " + "2.2.5.1 try to get userstatus again after login action");
          UserStatus stat = SMTHHelper.queryActiveUserStatus().blockingFirst();
          //Log.d(TAG, "call: " + stats.size());
          return stat;
        } else {
          return userStatus;
        }
      }
    }).map(new Function<UserStatus, UserStatus>() {
      @Override public UserStatus apply(@NonNull UserStatus userStatus) throws Exception {
        //Log.d(TAG, "3.0 call: " + userStatus.toString());
        String userid = userStatus.getId();
        if (userid != null && !TextUtils.equals(userid, "guest")) {
          // valid logined user
          if (SMTHApplication.activeUser != null && TextUtils.equals(userid, SMTHApplication.activeUser.getId())) {
            // current user is already cached in SMTHApplication
            //Log.d(TAG, "call: " + "3.1 New user is the same with cached user, copy faceURL from local");
            userStatus.setFace_url(SMTHApplication.activeUser.getFace_url());
          } else {
            // get correct faceURL
            //Log.d(TAG, "call: " + "3.2 New user is different with cached user, get real face URL from remote");
            UserInfo userInfo = helper.wService.queryUserInformation(userid).blockingFirst();
            if (userInfo != null) {
              userStatus.setFace_url(userInfo.getFace_url());
            }
          }
        } else {
          //Log.d(TAG, "call: 3.3 " + "invalid logined user");
        }
        return userStatus;
      }
    }).observeOn(Schedulers.io()).subscribeOn(Schedulers.io()).subscribe(new Observer<UserStatus>() {
      @Override public void onSubscribe(@NonNull Disposable disposable) {

      }

      @Override public void onNext(@NonNull UserStatus userStatus) {
        //Log.d(TAG, "4.0 onNext: " + userStatus.toString());
        String userid = userStatus.getId();
        if (userid == null || TextUtils.equals(userid, "guest")) return;

        // cache user if necessary, so we don't have to query User avatar url again in the future
        boolean updateUserIcon = false;
        if (!SMTHApplication.isValidUser()) {
          // Log.d(TAG, "onNext: " + "4.1 cache userStatus as activeUser");
          SMTHApplication.activeUser = userStatus;
          updateUserIcon = true;
        }

        // send notification: 1. new message 2. new activeUser to update Sidebar status
        String message = getNotificationMessage(userStatus);
        if ((updateUserIcon || message.length() > 0) && userStatusReceiver != null) {
          //Log.d(TAG, "4.2 cached user, valid message, valid receiver, send message");
          Bundle bundle = new Bundle();
          if(message.length() > 0) {
            bundle.putString(SMTHApplication.SERVICE_NOTIFICATION_MESSAGE, message);
          }
          // Here we call send passing a resultCode and the bundle of extras
          userStatusReceiver.send(Activity.RESULT_OK, bundle);
        }
      }

      @Override public void onError(@NonNull Throwable e) {
        Log.e(TAG, "onError: " + Log.getStackTraceString(e));
      }

      @Override public void onComplete() {

      }
    }); // .subscribe
  }
}
