package com.zfdang.zsmth_android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.newsmth.AjaxResponse;
import com.zfdang.zsmth_android.newsmth.SMTHHelper;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

/**
 * A login screen that offers login to newsmth forum
 */
public class LoginActivity extends SMTHBaseActivity implements OnClickListener {

  private EditText m_userNameEditText;
  private EditText m_passwordEditText;
  private CheckBox mAutoLogin;

  private final String TAG = "LoginActivity";

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    // these two variables should be loaded from preference
    Settings setting = Settings.getInstance();
    String username = setting.getUsername();
    String password = setting.getPassword();
    boolean autologin = setting.isAutoLogin();

    m_userNameEditText = (EditText) findViewById(R.id.username_edit);
    m_userNameEditText.setText(username);
    m_passwordEditText = (EditText) findViewById(R.id.password_edit);
    m_passwordEditText.setText(password);

    mAutoLogin = (CheckBox) findViewById(R.id.auto_login);
    mAutoLogin.setChecked(autologin);

    TextView registerLink = (TextView) findViewById(R.id.register_link);
    registerLink.setMovementMethod(LinkMovementMethod.getInstance());

    TextView asmHelpLink = (TextView) findViewById(R.id.asm_help_link);
    asmHelpLink.setMovementMethod(LinkMovementMethod.getInstance());

    Button ubutton = (Button) findViewById(R.id.signin_button);
    ubutton.setOnClickListener(this);

    // enable back button in the title barT
    ActionBar bar = getSupportActionBar();
    if (bar != null) {
      bar.setDisplayHomeAsUpEnabled(true);
    }
  }

  @Override public void onClick(View view) {
    if (view.getId() == R.id.signin_button) {
      // login with provided username and password
      String username = m_userNameEditText.getText().toString();
      String password = m_passwordEditText.getText().toString();

      boolean cancel = false;
      View focusView = null;

      // Check for a valid password, if the user entered one.
      // this code should be refined
      if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
        focusView = m_passwordEditText;
        cancel = true;
      }

      // Check for a valid username.
      if (TextUtils.isEmpty(username)) {
        focusView = m_userNameEditText;
        cancel = true;
      } else if (!isUsernameValid(username)) {
        focusView = m_userNameEditText;
        cancel = true;
      }

      if (cancel) {
        // There was an error; don't attempt login and focus the first
        // form field with an error.
        focusView.requestFocus();
      } else {
        Settings.getInstance().setAutoLogin(mAutoLogin.isChecked());
        Settings.getInstance().setLastLoginSuccess(false);
        attemptLoginFromWWW(username, password);
      }
    }
  }

  // login from WWW, then nforum / www / m are all logined
  private void attemptLoginFromWWW(final String username, final String password) {
    // perform the user login attempt.
    showProgress("登录中...");

    Log.d(TAG, "start login now...");
    // use attempt to login, so set userOnline = true
    Settings.getInstance().setUserOnline(true);

    // RxJava & Retrofit: VERY VERY good article
    // http://gank.io/post/560e15be2dca930e00da1083
    SMTHHelper helper = SMTHHelper.getInstance();
    // clear cookies upon login
    helper.mCookieJar.clear();
    final String cookieDays = "2";
    helper.wService.login(username, password, cookieDays)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<AjaxResponse>() {
          @Override public void onSubscribe(@NonNull Disposable disposable) {

          }

          @Override public void onNext(@NonNull AjaxResponse ajaxResponse) {
            dismissProgress();
            // {"ajax_st":0,"ajax_code":"0101","ajax_msg":"您的用户名并不存在，或者您的密码错误"}
            // {"ajax_st":0,"ajax_code":"0105","ajax_msg":"请勿频繁登录"}
            // {"ajax_st":1,"ajax_code":"0005","ajax_msg":"操作成功"}
            switch (ajaxResponse.getAjax_st()) {
              case AjaxResponse.AJAX_RESULT_OK:
                Toast.makeText(getApplicationContext(), "登录成功!", Toast.LENGTH_SHORT).show();

                // save username & passworld
                Settings.getInstance().setUsername(username);
                Settings.getInstance().setPassword(password);
                Settings.getInstance().setLastLoginSuccess(true);

                Intent resultIntent = new Intent();
                setResult(Activity.RESULT_OK, resultIntent);
                finish();
                break;
              default:
                Toast.makeText(SMTHApplication.getAppContext(), ajaxResponse.toString(), Toast.LENGTH_LONG).show();
                break;
            }
          }

          @Override public void onError(@NonNull Throwable e) {
            dismissProgress();
            Toast.makeText(SMTHApplication.getAppContext(), "登录失败!\n" + e.toString(), Toast.LENGTH_LONG).show();
          }

          @Override public void onComplete() {

          }
        });
  }

  private boolean isUsernameValid(String username) {
    return username.length() > 0;
  }

  private boolean isPasswordValid(String password) {
    return password.length() > 0;
  }

  @Override public void onStart() {
    super.onStart();
  }

  @Override public void onStop() {
    super.onStop();
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    switch (item.getItemId()) {
      case android.R.id.home:
        onBackPressed();
        return true;
      default:
        return super.onOptionsItemSelected(item);
    }
  }
}

