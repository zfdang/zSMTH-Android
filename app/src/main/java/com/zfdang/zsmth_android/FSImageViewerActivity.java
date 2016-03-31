package com.zfdang.zsmth_android;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import me.relex.circleindicator.CircleIndicator;

/**
 * An example full-screen activity that shows and hides the system UI (i.e.
 * status bar and navigation/system bar) with user interaction.
 */
public class FSImageViewerActivity extends AppCompatActivity implements View.OnClickListener{

    private static final String TAG = "FullViewer";
    private static final boolean AUTO_HIDE = true;
    private static final int AUTO_HIDE_DELAY_MILLIS = 3000;

    private final Handler mHideHandler = new Handler();
    private boolean isFullscreen;
    private ViewPager mViewPager;

    private final Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            hide();
        }
    };
    private Button mButton;
    private FSImagePagerAdapter mPagerAdapter;
    private CircleIndicator mIndicator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fs_image_viewer);
        getSupportActionBar().hide();

        isFullscreen = false;
        mViewPager = (ViewPager) findViewById(R.id.fullscreen_image_pager);

        mPagerAdapter = new FSImagePagerAdapter(null, this);
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setCurrentItem(2);

        mIndicator = (CircleIndicator) findViewById(R.id.fullscreen_image_indicator);
        mIndicator.setViewPager(mViewPager);

//        mViewPager.setOnSystemUiVisibilityChangeListener(new View.OnSystemUiVisibilityChangeListener() {
//            @Override
//            public void onSystemUiVisibilityChange(int visibility) {
//                if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
//                    Log.d(TAG, "NOT in Full Screen");
//                    isFullscreen = false;
//                } else {
//                    isFullscreen = true;
//                }
//            }
//        });


        mButton = (Button) findViewById(R.id.fullscreen_image_switch);
        // Set up the user interaction to manually show or hide the system UI.
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.d(TAG, "onClick: ");
                toggle();
            }
        });
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
    public void onClick(View v) {

    }
}
