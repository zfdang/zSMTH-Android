package com.zfdang.zsmth_android;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

import com.zfdang.SMTHApplication;

import java.util.ArrayList;
import java.util.List;

import me.relex.circleindicator.CircleIndicator;
import uk.co.senab.photoview.PhotoViewAttacher;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FSImageViewerActivity extends AppCompatActivity implements PhotoViewAttacher.OnPhotoTapListener, View.OnLongClickListener{

    private static final String TAG = "FullViewer";
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    private final Handler mHideHandler = new Handler();
    private boolean isFullscreen;
    private HackyViewPager mViewPager;

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private FSImagePagerAdapter mPagerAdapter;
    private CircleIndicator mIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fs_image_viewer);
        getSupportActionBar().hide();

        isFullscreen = false;
        mViewPager = (HackyViewPager) findViewById(R.id.fullscreen_image_pager);


        // find paramenters from parent
        ArrayList<String> URLs = getIntent().getStringArrayListExtra(SMTHApplication.ATTACHMENT_URLS);
        assert URLs != null;
        int pos = getIntent().getIntExtra(SMTHApplication.ATTACHMENT_CURRENT_POS, 0);
        if(pos <0 || pos >= URLs.size()) {
            pos = 0;
        }

        mPagerAdapter = new FSImagePagerAdapter(URLs, this);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(pos);

        mIndicator = (CircleIndicator) findViewById(R.id.fullscreen_image_indicator);
        mIndicator.setViewPager(mViewPager);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        // TODO Auto-generated method stub
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() + 1, true);
            return true;
        } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            mViewPager.setCurrentItem(mViewPager.getCurrentItem() - 1, true);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // http://stackoverflow.com/questions/4500354/control-volume-keys
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // disable the beep sound when volume up/down is pressed
        if ((keyCode == KeyEvent.KEYCODE_VOLUME_UP) || (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }


    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        // Trigger the initial hide() shortly after the activity has been
        // created, to briefly hint to the user that UI controls
        // are available.
        delayedHide(AUTO_HIDE_DELAY_MILLIS);
    }


    private void toggle() {
        if (isFullscreen) {
            show();
        } else {
            hide();
        }
    }

    private void hide() {
        // Hide status bar and navigation bar
        mViewPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        isFullscreen = true;
    }


    @SuppressLint("InlinedApi")
    private void show() {
        // Show the status bar
        mViewPager.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
        isFullscreen = false;
    }

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any
     * previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }

    @Override
    public boolean onLongClick(View v) {
        Log.d(TAG, "onLongClick: ");
//        updateImageInfoByIndex();

        // build menu for long click
        List<String> itemList = new ArrayList<String>();
        itemList.add(getString(R.string.full_image_information));
        itemList.add(getString(R.string.full_image_save));
        itemList.add(getString(R.string.full_image_back));
        final String[] items = new String[itemList.size()];
        itemList.toArray(items);

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(String.format("图片: %s", "unknown"));
        builder.setItems(items, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int item) {
                switch (item) {
                    case 0:
//                        showExifDialog();
                        break;
                    case 1:
//                        saveImage();
                        break;
                    case 2:
                        dialog.dismiss();
                        break;
                    default:
                        break;
                }
                dialog.dismiss();
            }
        });

        builder.create().show();

        return true;
    }

    @Override
    public void onPhotoTap(View view, float x, float y) {
        toggle();
    }

    @Override
    public void onOutsidePhotoTap() {
    }
}
