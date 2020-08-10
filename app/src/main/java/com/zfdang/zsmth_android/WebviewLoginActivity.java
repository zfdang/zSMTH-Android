package com.zfdang.zsmth_android;

import android.os.Bundle;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class WebviewLoginActivity extends SMTHBaseActivity {

    private WebView mWebView;
    private String url = "https://www.newsmth.net/";
//    private String url = "https://wap.newsmth.net/login";

    private String username;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_login);

        // get username & password
        Bundle extras = getIntent().getExtras();
        username = extras.getString(LoginActivity.USERNAME);
        password = extras.getString(LoginActivity.PASSWORD);

        mWebView = (WebView) findViewById(R.id.webview_login);
        mWebView.getSettings().setAppCacheEnabled(false);
        mWebView.getSettings().setCacheMode(WebSettings.LOAD_NO_CACHE);
        mWebView.getSettings().setDatabaseEnabled(false);
        mWebView.getSettings().setDomStorageEnabled(false );
        mWebView.getSettings().setGeolocationEnabled(false);
        mWebView.getSettings().setSaveFormData(true);
        mWebView.getSettings().setSavePassword(false);
        mWebView.getSettings().setJavaScriptEnabled(true); ///------- 设置javascript 可用

        // https://stackoverflow.com/questions/9602124/enable-horizontal-scrolling-in-a-webview
//        mWebView.getSettings().setLoadWithOverviewMode(true);
        mWebView.getSettings().setUseWideViewPort(true);
        mWebView.setInitialScale(100);

        mWebView.setWebChromeClient(new WebChromeClient()
        {
            @Override
            public void onProgressChanged(WebView view, int newProgress)
            {
                super.onProgressChanged(view, newProgress);
                view.requestFocus();
            }
        });
        mWebView.setWebViewClient(new WebviewLoginClient(this, username, password));
        mWebView.loadUrl(url);
    }
}