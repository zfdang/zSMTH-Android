package com.zfdang.zsmth_android;

import android.os.Bundle;
import android.util.Log;
import android.webkit.ConsoleMessage;
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

        try {
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

            // Ensure user agent has a modern token to avoid some sites returning mobile-empty pages
            String ua = webSettings.getUserAgentString();
            if (ua == null || !ua.contains("Mobile")) {
                webSettings.setUserAgentString(ua + " Mobile-SMTH-App");
            }

            // Only set hardware layer on API 17+ to be safe
            if (android.os.Build.VERSION.SDK_INT >= 17) {
                mWebView.setLayerType(android.view.View.LAYER_TYPE_HARDWARE, null);
            }

        } catch (Throwable t) {
            Log.e(TAG, "Exception while configuring WebView settings", t);
        }

        mWebView.setWebChromeClient(new WebChromeClient()
        {
            @Override
            public void onProgressChanged(WebView view, int newProgress)
            {
                super.onProgressChanged(view, newProgress);
                view.requestFocus();
            }

            @Override
            public boolean onConsoleMessage(ConsoleMessage consoleMessage) {
                Log.i(TAG, "Console: " + consoleMessage.message() + " -- " + consoleMessage.sourceId() + ":" + consoleMessage.lineNumber());
                return super.onConsoleMessage(consoleMessage);
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

        try {
            mWebView.loadUrl(url);
        } catch (Throwable t) {
            Log.e(TAG, "Exception while loading URL", t);
            // Show a simple fallback message instead of a white screen
            if (mWebView != null) {
                mWebView.loadData("<html><body><h3>加载失败，请检查网络或重试</h3></body></html>", "text/html", "UTF-8");
            }
        }
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
