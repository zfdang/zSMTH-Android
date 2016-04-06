package com.zfdang.zsmth_android;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
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

import com.zfdang.zsmth_android.newsmth.SMTHHelper;

import okhttp3.ResponseBody;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


/**
 * A login screen that offers login to newsmth forum
 */
public class LoginActivity extends AppCompatActivity implements OnClickListener {

    private EditText m_userNameEditText;
    private EditText m_passwordEditText;
    private CheckBox mAutoLogin;

    private ProgressDialog pdialog = null;
    private final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // these two variables should be loaded from preference
        Settings setting = Settings.getInstance();
        String username = setting.getUsername();
        String password =  setting.getPassword();
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
        if(bar != null){
            bar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public void onClick(View view) {
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
        showProgress(true);

        Log.d(TAG, "start login now...");
        // use attempt to login, so set userOnline = true
        Settings.getInstance().setUserOnline(true);

        // RxJava & Retrofit: VERY VERY good article
        // http://gank.io/post/560e15be2dca930e00da1083
        SMTHHelper helper = SMTHHelper.getInstance();
        helper.wService.loginWithKick(username, password, "on")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .map(new Func1<ResponseBody, Integer>() {
                    // parse response body, and convert it to a flag
                    // 0: success
                    // 1: wrong username / password
                    // 2: decoding exception
                    // 3. network unreachable  -- this is actually hanndled by onErrorAction of Subscriber
                    @Override
                    public Integer call(ResponseBody response) { // 参数类型 String
                        try {
                            String resp = SMTHHelper.DecodeResponseFromWWW(response.bytes());
                            // 0. 登陆成功
                            // 1. 用户密码错误，请重新登录
                            // 2. 登录过于频繁
                            // 3. unknown reason
                            return SMTHHelper.parseResultOfLoginFromWWW(resp);
                        } catch (Exception e) {
                            Log.d(TAG, "call: " + Log.getStackTraceString(e));
                            return 3;
                        }
                    }
                })
                .subscribe(new Subscriber<Integer>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "onCompleted: ");
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "call: " + Log.getStackTraceString(e));
                        showProgress(false);
                        Toast.makeText(getApplicationContext(), "连接错误，请检查网络.", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNext(Integer integer) {
                        showProgress(false);
                        switch (integer) {
                            case SMTHHelper.LOGIN_RESULT_OK:
                                Toast.makeText(getApplicationContext(), "登录成功!", Toast.LENGTH_SHORT).show();

                                // save username & passworld
                                Settings.getInstance().setUsername(username);
                                Settings.getInstance().setPassword(password);
                                Settings.getInstance().setLastLoginSuccess(true);

                                Intent resultIntent = new Intent();
                                setResult(Activity.RESULT_OK, resultIntent);
                                finish();
                                break;
                            case SMTHHelper.LOGIN_RESULT_FAILED:
                                Toast.makeText(getApplicationContext(), "您的用户名并不存在，或者您的密码错误!", Toast.LENGTH_LONG).show();
                                break;
                            case SMTHHelper.LOGIN_RESULT_TOO_FREQUENT:
                                Toast.makeText(getApplicationContext(), "请勿频繁登录", Toast.LENGTH_LONG).show();
                                break;
                            case SMTHHelper.LOGIN_RESULT_UNKNOWN:
                                Toast.makeText(getApplicationContext(), "未知错误，请稍后重试...", Toast.LENGTH_LONG).show();
                                break;
                        }
                    }
                });
    }

    private boolean isUsernameValid(String username) {
        return username.length() > 4;
    }

    private boolean isPasswordValid(String password) {
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    private void showProgress(final boolean show) {
        if (pdialog == null) {
            pdialog = new ProgressDialog(this);
        }
        if (show) {
            pdialog.setMessage("登录中...");
            pdialog.show();
        } else {
            pdialog.cancel();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
    }

    @Override
    public void onStop() {
        super.onStop();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}

