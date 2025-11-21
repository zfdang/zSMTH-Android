package com.zfdang.zsmth_android;

import android.os.Bundle;
import android.util.Log;
import android.webkit.RenderProcessGoneDetail;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

public class WebviewLoginActivity extends SMTHBaseActivity {

    private static final String TAG = "WebviewLoginActivity";
    private WebView mWebView;
    private String url = "https://m.newsmth.net/index";

    private String username;
    private String password;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_webview_login);

        // get username & password
        Bundle extras = getIntent().getExtras();
        if(extras != null) {
            username = extras.getString(LoginActivity.USERNAME);
            password = extras.getString(LoginActivity.PASSWORD);
        }

        mWebView = (WebView) findViewById(R.id.webview_login);

        WebSettings webSettings = mWebView.getSettings();
        webSettings.setCacheMode(WebSettings.LOAD_DEFAULT);
        webSettings.setGeolocationEnabled(false);
        webSettings.setSaveFormData(false);
        webSettings.setSavePassword(false);
        webSettings.setJavaScriptEnabled(true); ///------- 设置javascript 可用

        // https://stackoverflow.com/questions/9602124/enable-horizontal-scrolling-in-a-webview
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
//        mWebView.setInitialScale(120);
//        mWebView.getSettings().setTextZoom(200);

        //支持屏幕缩放
        webSettings.setSupportZoom(true);
        webSettings.setBuiltInZoomControls(true);

        // Android 16兼容性修复：启用必要的WebView功能
        // Fix for Android 16: Enable necessary WebView features
        webSettings.setDomStorageEnabled(true);  // 启用DOM存储，现代网页必需
        webSettings.setDatabaseEnabled(true);     // 启用数据库存储

        // 允许混合内容 (需要 API 21+)
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            webSettings.setMixedContentMode(android.webkit.WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        // Android 16兼容性修复：确保WebView渲染器不会因为错误而白屏
        // Fix for Android 16: Ensure WebView renderer doesn't crash
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            webSettings.setSafeBrowsingEnabled(true);
        }

        // 启用硬件加速以提高性能（Android 16推荐）
        // Enable hardware acceleration for better performance (recommended for Android 16)
        mWebView.setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null);

        mWebView.setWebChromeClient(new WebChromeClient()
        {
            @Override
            public void onProgressChanged(WebView view, int newProgress)
            {
                super.onProgressChanged(view, newProgress);
                view.requestFocus();
            }
        });

        // 创建自定义的 WebViewClient 并添加渲染器崩溃处理
        WebviewLoginClient webViewClient = new WebviewLoginClient(this, username, password) {
            @Override
            public boolean onRenderProcessGone(WebView view, RenderProcessGoneDetail detail) {
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
                    Log.e(TAG, "WebView渲染器进程崩溃 - didCrash: " + detail.didCrash());
                    // 清理当前的 WebView
                    if (mWebView != null) {
                        mWebView.destroy();
                        mWebView = null;
                    }
                    // 通知用户并关闭活动
                    runOnUiThread(() -> {
                        android.widget.Toast.makeText(WebviewLoginActivity.this,
                            "WebView加载失败，请重试",
                            android.widget.Toast.LENGTH_LONG).show();
                        finish();
                    });
                    return true; // 表示我们已经处理了崩溃
                }
                return super.onRenderProcessGone(view, detail);
            }
        };

        mWebView.setWebViewClient(webViewClient);
        mWebView.loadUrl(url);
    }

    @Override
    protected void onDestroy() {
        // 正确清理 WebView 以防止内存泄漏和崩溃
        if (mWebView != null) {
            mWebView.stopLoading();
            mWebView.setWebViewClient(null);
            mWebView.setWebChromeClient(null);
            mWebView.destroy();
            mWebView = null;
        }
        super.onDestroy();
    }
}
