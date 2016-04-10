package com.zfdang.zsmth_android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.fresco.WrapContentDraweeView;
import com.zfdang.zsmth_android.helpers.KeyboardLess;
import com.zfdang.zsmth_android.helpers.StringUtils;
import com.zfdang.zsmth_android.newsmth.SMTHHelper;
import com.zfdang.zsmth_android.newsmth.UserInfo;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class QueryUserActivity extends AppCompatActivity {
    private static final String TAG = "QueryUserActivity";
    private String mUsername;
    private ProgressDialog pdialog = null;

    private WrapContentDraweeView mImageView;

    private TextView mUserId;
    private TextView mUserNickname;
    private TextView mUserGender;
    private TextView mUserConstellation;
    private TextView mUserQq;
    private TextView mUserHomepage;

    private TextView mUserLevel;
    private TextView mUserTotalpost;
    private TextView mUserLogincount;
    private TextView mUserLife;
    private TextView mUserScore;
    private TextView mUserFirstLogintime;
    private TextView mUserLastLogintime;
    private TextView mUserLoginip;
    private TextView mUserCurrentstatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_query_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);;


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        assignViews();

        findViewById(R.id.query_user_action_query).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                EditText tv = (EditText) findViewById(R.id.query_user_input);
                String userid = tv.getText().toString();
                if(userid.length() > 0) {
                    mUsername = userid;
                    KeyboardLess.$hide(QueryUserActivity.this, tv);
                    tv.clearFocus();
                    LoadUserInfo();
                }

            }
        });


        // get Board information from launcher
        Intent intent = getIntent();
        String username = intent.getStringExtra(SMTHApplication.QUERY_USER_INFO);
        assert username != null;
        mUsername = username;
        LoadUserInfo();
    }


    private void assignViews() {
        mImageView = (WrapContentDraweeView) findViewById(R.id.imageView);

        mUserId = (TextView) findViewById(R.id.query_user_id);
        mUserNickname = (TextView) findViewById(R.id.query_user_nickname);
        mUserGender = (TextView) findViewById(R.id.query_user_gender);
        mUserConstellation = (TextView) findViewById(R.id.query_user_constellation);
        mUserQq = (TextView) findViewById(R.id.query_user_qq);
        mUserHomepage = (TextView) findViewById(R.id.query_user_homepage);

        mUserLevel = (TextView) findViewById(R.id.query_user_level);
        mUserTotalpost = (TextView) findViewById(R.id.query_user_totalpost);
        mUserLogincount = (TextView) findViewById(R.id.query_user_logincount);
        mUserLife = (TextView) findViewById(R.id.query_user_life);
        mUserScore = (TextView) findViewById(R.id.query_user_score);
        mUserFirstLogintime = (TextView) findViewById(R.id.query_user_first_logintime);
        mUserLastLogintime = (TextView) findViewById(R.id.query_user_last_logintime);
        mUserLoginip = (TextView) findViewById(R.id.query_user_loginip);
        mUserCurrentstatus = (TextView) findViewById(R.id.query_user_currentstatus);

    }

    public void showProgress(String message, final boolean show) {
        if(pdialog == null) {
            pdialog = new ProgressDialog(this);
        }
        if (show) {
            pdialog.setMessage(message);
            pdialog.show();
        } else {
            pdialog.cancel();
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.query_user_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int code = item.getItemId();
        if(code == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }


    public void LoadUserInfo() {

        showProgress("加载用户信息中...", true);
        SMTHHelper helper = SMTHHelper.getInstance();
        helper.wService.queryUserInformation(mUsername)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UserInfo>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        showProgress("", false);
                        Toast.makeText(QueryUserActivity.this, "加载失败！", Toast.LENGTH_SHORT).show();
                        Log.d(TAG, "onError: " + Log.getStackTraceString(e));
                    }

                    @Override
                    public void onNext(UserInfo user) {
                        Log.d(TAG, "onNext: " + user.toString());

                        mUserId.setText(user.getId());
                        mUserNickname.setText(user.getUser_name());
                        mUserGender.setText(user.getGender());
                        mUserConstellation.setText(user.getAstro());
                        mUserQq.setText(user.getQq());
                        mUserHomepage.setText(user.getHome_page());

                        mUserLevel.setText(user.getLevel());
                        mUserTotalpost.setText(user.getPost_count());
                        mUserLogincount.setText(user.getLogin_count());
                        mUserLife.setText(user.getLifeDesc());
                        mUserScore.setText(user.getScore_user());
                        mUserFirstLogintime.setText(user.getFirst_login_time());
                        mUserLastLogintime.setText(user.getLast_login_time());
                        mUserLoginip.setText(StringUtils.lookupIPLocationInProfile(user.getLast_login_ip()));
                        mUserCurrentstatus.setText(user.getStatus());

                        if(user.getFace_url() != null && user.getFace_url().length() > 10) {
                            mImageView.setImageFromStringURL(user.getFace_url());
                        }
                        showProgress("", false);

                    }


                });
    }
}
