package com.zfdang.zsmth_android;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.webkit.ValueCallback;
import android.webkit.WebResourceResponse;
import android.webkit.WebView;
import android.webkit.WebViewClient;

public class WebviewLoginClient extends WebViewClient {

    private static final String TAG = "WebviewLoginClient";
    private String username;
    private String password;

    Activity activity;

    public WebviewLoginClient(Activity activity, String username, String password) {
        this.activity = activity;
        this.username = username;
        this.password = password;
    }

    public boolean shouldOverrideUrlLoading(WebView view, String url) {
        if (url.startsWith("https://www.newsmth.net/nForum/")) {
            Intent resultIntent = new Intent();
            activity.setResult(Activity.RESULT_OK, resultIntent);
            activity.finish();
        }
        return false;
    }

    @Override
    public WebResourceResponse shouldInterceptRequest(WebView view, String url) {
        return null;
    }

    public void onPageFinished(WebView view, String url) {
        if (url.equals("https://www.newsmth.net/")) {
            // login page, input id and passwd automatically
            final String js = "javascript: " +
                    "document.getElementById('id').value = '" + this.username + "';" +
                    "document.getElementById('pwd').value = '" + this.password + "';" +
                    "var cookies = document.getElementsByName('CookieDate');" +
                    "cookies[0].style.display = 'none';" +
                    "document.getElementById('s-mode').style.display = 'none';" +
                    "document.getElementById('b_reset').style.display = 'none';" +
                    "document.getElementById('b_guest').style.display = 'none';" +
                    "document.getElementById('b_reg').style.display = 'none';";

            if (Build.VERSION.SDK_INT >= 19) {
                view.evaluateJavascript(js, new ValueCallback<String>() {
                    @Override
                    public void onReceiveValue(String s) {
                    }
                });
            } else {
                view.loadUrl(js);
            }
        }
        super.onPageFinished(view, url);
    }
}
