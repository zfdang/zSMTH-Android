package com.zfdang.zsmth_android;

import android.support.v7.app.ActionBar;
import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zfdang.zsmth_android.newsmth.SMTHHelper;

import okhttp3.ResponseBody;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;


/**
 * A login screen that offers login to newsmth forum
 */
public class LoginActivity extends AppCompatActivity implements OnClickListener {

    private EditText m_userNameEditText;
    private EditText m_passwordEditText;

    private ProgressDialog pdialog = null;
    private final String TAG = "Login Activity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // these two variables should be loaded from preference
        String username = "mozilla";
        String password = "hello";

        m_userNameEditText = (EditText) findViewById(R.id.username_edit);
        m_userNameEditText.setText(username);
        m_passwordEditText = (EditText) findViewById(R.id.password_edit);
        m_passwordEditText.setText(password);

        TextView registerLink = (TextView) findViewById(R.id.register_link);
        registerLink.setMovementMethod(LinkMovementMethod.getInstance());

        TextView asmHelpLink = (TextView) findViewById(R.id.asm_help_link);
        asmHelpLink.setMovementMethod(LinkMovementMethod.getInstance());

        Button ubutton = (Button) findViewById(R.id.signin_button);
        ubutton.setOnClickListener(this);

        Button gbutton = (Button) findViewById(R.id.guest_button);
        gbutton.setOnClickListener(this);

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
                return;
            } else {
                attemptLogin(username, password);
            }
        } else if (view.getId() == R.id.guest_button) {
            // login with guest name
            attemptLogin("guest", "");
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin(final String username, final String password) {

        // Show a progress spinner, and kick off a background task to
        // perform the user login attempt.
        showProgress(true);

        Log.d(TAG, "start login now...");

        // RxJava & Retrofit: VERY VERY good article
        // http://gank.io/post/560e15be2dca930e00da1083
        SMTHHelper helper = SMTHHelper.getInstance();
        helper.wService.loginWithKick(username, password, "1")
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
                            String resp = SMTHHelper.DecodeWWWResponse(response.bytes());
                            Log.d(TAG, resp);

                            if (resp.contains("你登录的窗口过多")) {
                                Log.d(TAG, "too many login user, this should not happend since we are using loginWithKick");
                                return 0;
                            } else if (resp.contains("用户密码错误")) {
                                return 1;
                            } else if(resp.contains("window.location.href")){
                                // successful login, user is redirected to frames.html
                                return 0;
                            }
                            return 2;
                        } catch (Exception e) {
                            Log.d(TAG, e.toString());
                            return 2;
                        }
                    }
                })
        // 自动创建 Subscriber ，并使用 onNextAction、 onErrorAction 和 onCompletedAction 来定义 onNext()、 onError() 和 onCompleted()
        // observable.subscribe(onNextAction, onErrorAction, onCompletedAction);
        .subscribe(new Action1<Integer>() {
            // onNextAction
            @Override
            public void call(Integer code) {
                Log.d(TAG, code.toString());

                showProgress(false);
                switch (code) {
                    case 0:
                        Toast.makeText(getApplicationContext(), "登录成功!", Toast.LENGTH_SHORT).show();
                        break;
                    case 1:
                        Toast.makeText(getApplicationContext(), "用户名或者密码错误.", Toast.LENGTH_LONG).show();
                        break;
                    case 2:
                        Toast.makeText(getApplicationContext(), "未知错误，请稍后重试...", Toast.LENGTH_LONG).show();
                        break;
                }
            }
        }, new Action1<Throwable>() {
            // onErrorAction
            @Override
            public void call(Throwable throwable) {
                Log.d(TAG, throwable.toString());
                showProgress(false);
                Toast.makeText(getApplicationContext(), "连接错误，请检查网络.", Toast.LENGTH_LONG).show();
            }
        }, new Action0() {
            // onCompletedAction
            @Override
            public void call() {
                Log.d(TAG, "Completed");
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

