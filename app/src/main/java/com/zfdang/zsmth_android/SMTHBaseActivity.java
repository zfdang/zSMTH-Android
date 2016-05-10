package com.zfdang.zsmth_android;

import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;

/**
 * Created by zfdang on 2016-5-10.
 */
public class SMTHBaseActivity extends AppCompatActivity {
    protected ProgressDialog pDialog = null;

    // http://stackoverflow.com/questions/22924825/view-not-attached-to-window-manager-crash
    public void showProgress(String message) {
        if(pDialog == null) {
            pDialog = new ProgressDialog(this, R.style.PDialog_MyTheme);
            pDialog.setCancelable(false);
            pDialog.setCanceledOnTouchOutside(false);
        }
        pDialog.setMessage(message);
        pDialog.show();
    }


    public void dismissProgress() {
        if(pDialog != null && pDialog.isShowing()) {
            pDialog.dismiss();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();

        dismissProgress();
    }
}
