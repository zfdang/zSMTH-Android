package com.zfdang.zsmth_android;

import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.webkit.WebView;
import android.widget.Toast;

import com.jude.swipbackhelper.SwipeBackHelper;
import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.newsmth.AjaxResponse;
import com.zfdang.zsmth_android.newsmth.SMTHHelper;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MailContentActivity extends AppCompatActivity {

    private static final String TAG = "MailContent";
    private String mail_url;
    private WebView webview;

    @Override
    protected void onDestroy() {
        super.onDestroy();
        SwipeBackHelper.onDestroy(this);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        SwipeBackHelper.onPostCreate(this);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        SwipeBackHelper.onCreate(this);

        setContentView(R.layout.activity_mail_content);

        // init webview
        webview = (WebView) findViewById(R.id.mail_content_webview);
        webview.loadData("加载中...", "text/html; charset=utf-8", "UTF-8");

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // load mail content
        Bundle bundle = getIntent().getExtras();
        mail_url = bundle.getString(SMTHApplication.MAIL_URL_OBJECT);
        loadMailContent();
    }

    public void loadMailContent() {
        SMTHHelper helper = SMTHHelper.getInstance();
        helper.wService.getMailContent(mail_url)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<AjaxResponse>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: " + Log.getStackTraceString(e) );
                    }

                    @Override
                    public void onNext(AjaxResponse ajaxResponse) {
                        Log.d(TAG, "onNext: " + ajaxResponse.getContent());
                        webview.loadData(ajaxResponse.getContent(), "text/html; charset=utf-8", "UTF-8");
                    }
                });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.mail_content_reply) {
            Toast.makeText(MailContentActivity.this, "Reply", Toast.LENGTH_SHORT).show();
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mail_content_menu, menu);
        return true;
    }

}
