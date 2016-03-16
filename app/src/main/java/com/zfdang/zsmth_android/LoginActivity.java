package com.zfdang.zsmth_android;

import android.app.ProgressDialog;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;

import android.os.AsyncTask;

import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;


/**
 * A login screen that offers login to newsmth forum
 */
public class LoginActivity extends AppCompatActivity implements OnClickListener {
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    private EditText m_userNameEditText;
    private EditText m_passwordEditText;

    private Handler m_handler = new Handler();
    private ProgressDialog pdialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

//        String username = aSMApplication.getCurrentApplication().getAutoUserName();
//        String password = aSMApplication.getCurrentApplication().getAutoPassword();

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
    }

    @Override
    public void onClick(View view) {
        if (view.getId() == R.id.signin_button) {
            // login with provided username and password
            attemptLogin();
        } else if (view.getId() == R.id.guest_button) {
            // login with guest name
        }

    }


    public void showSuccessToast() {
        m_handler.post(new Runnable() {
            public void run() {
                Toast.makeText(getApplicationContext(), "登录成功.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showAuthenticationFailedToast() {
        m_handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "用户名或密码错.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void showConnectionFailedToast() {
        m_handler.post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(getApplicationContext(), "连接错误，请检查网络.",
                        Toast.LENGTH_SHORT).show();
            }
        });
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            // login in progress
            return;
        }

        // Store values at the time of the login attempt.
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
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(username, password);
            mAuthTask.execute((Void) null);
        }
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
        if(pdialog == null) {
            pdialog = new ProgressDialog(this);
        }
        if(show) {
            pdialog.setMessage("登录中...");
            pdialog.show();
        } else {
            pdialog.cancel();
        }
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String username;
        private final String password;

        UserLoginTask(String username, String password) {
            this.username = username;
            this.password = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.
            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            // replace this with actual authentication process
            final String[] DUMMY_CREDENTIALS = new String[]{
                    "mozilla:hello", "dantifer:world"
            };
            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(username)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(password);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                showAuthenticationFailedToast();
                m_passwordEditText.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

