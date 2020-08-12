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

/**
 * A login screen that offers login to newsmth forum
 */
public class LoginActivity extends SMTHBaseActivity implements OnClickListener {

  private EditText m_userNameEditText;
  private EditText m_passwordEditText;
  private CheckBox mSaveInfo;

  static final int LOGIN_ACTIVITY_REQUEST_CODE = 9528;  // The request code
  static final String USERNAME = "USERNAME";
  static final String PASSWORD = "PASSWORD";

  private final String TAG = "LoginActivity";

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_login);

    // these two variables should be loaded from preference
    Settings setting = Settings.getInstance();

    boolean saveinfo = setting.isSaveInfo();
    mSaveInfo = (CheckBox) findViewById(R.id.save_info);
    mSaveInfo.setChecked(saveinfo);

    if(saveinfo) {
      String username = setting.getUsername();
      String password = setting.getPassword();
      m_userNameEditText = (EditText) findViewById(R.id.username_edit);
      m_userNameEditText.setText(username);
      m_passwordEditText = (EditText) findViewById(R.id.password_edit);
      m_passwordEditText.setText(password);
    }

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
      // verify username & password
      String username = m_userNameEditText.getText().toString();
      String password = m_passwordEditText.getText().toString();

      View focusView = null;
      // Check for a valid username.
      if (TextUtils.isEmpty(username)) {
        focusView = m_userNameEditText;
      } else if (TextUtils.isEmpty(password)) {
        // Check for a valid password
        focusView = m_passwordEditText;
      }

      if (focusView != null) {
        // There was an error; don't attempt login and focus the first
        // form field with an error.
        focusView.requestFocus();
      } else {
        // save info if selected
        boolean saveinfo = mSaveInfo.isChecked();
        Settings.getInstance().setSaveInfo(saveinfo);

        if(saveinfo) {
          // save
          Settings.getInstance().setUsername(username);
          Settings.getInstance().setPassword(password);
        } else {
          // clean existed
          Settings.getInstance().setUsername("");
          Settings.getInstance().setPassword("");
        }

        // continue to login with nforum web
        Intent intent = new Intent(this, WebviewLoginActivity.class);
        intent.putExtra(USERNAME, username);
        intent.putExtra(PASSWORD, password);
        startActivityForResult(intent, LOGIN_ACTIVITY_REQUEST_CODE);
      }
    }
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    super.onActivityResult(requestCode, resultCode, data);
    Log.d(TAG, "receive login result" + requestCode);
    if (requestCode == LOGIN_ACTIVITY_REQUEST_CODE) {
      Log.d(TAG, "receive login result");
      if (resultCode == RESULT_OK) {
        Toast.makeText(SMTHApplication.getAppContext(), "登录完成!", Toast.LENGTH_SHORT).show();

         if (SMTHApplication.activeUser != null) {
          SMTHApplication.activeUser.setId(Settings.getInstance().getUsername());
        }

        Intent resultIntent = new Intent();
        setResult(Activity.RESULT_OK, resultIntent);
        finish();
      }
     else
      {
        Settings.getInstance().setUsername("");
        Settings.getInstance().setPassword("");
    }
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

