package com.zfdang.zsmth_android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Parcelable;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.imagepipeline.core.ImagePipeline;
import com.mikepenz.aboutlibraries.LibsBuilder;
import com.umeng.analytics.MobclickAgent;
import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.fresco.WrapContentDraweeView;
import com.zfdang.zsmth_android.listeners.OnBoardFragmentInteractionListener;
import com.zfdang.zsmth_android.listeners.OnTopicFragmentInteractionListener;
import com.zfdang.zsmth_android.listeners.OnVolumeUpDownListener;
import com.zfdang.zsmth_android.models.Board;
import com.zfdang.zsmth_android.models.Mail;
import com.zfdang.zsmth_android.models.Topic;
import com.zfdang.zsmth_android.newsmth.AjaxResponse;
import com.zfdang.zsmth_android.newsmth.SMTHHelper;
import com.zfdang.zsmth_android.services.MaintainUserStatusService;
import com.zfdang.zsmth_android.services.UserStatusReceiver;

import java.lang.reflect.Field;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener,
        OnTopicFragmentInteractionListener,
        OnBoardFragmentInteractionListener,
        MailListFragment.OnListFragmentInteractionListener
//        SettingFragment.OnFragmentInteractionListener,
{
    // used by startActivityForResult
    static final int MAIN_ACTIVITY_REQUEST_CODE = 9527;  // The request code
    private static final String TAG = "MainActivity";
    // guidance fragment: display hot topics
    // this fragment is using RecyclerView to show all hot topics
    HotTopicFragment hotTopicFragment = null;
    FavoriteBoardFragment favoriteBoardFragment = null;
    AllBoardFragment allBoardFragment = null;
    MailListFragment mailListFragment = null;
    SettingFragment settingFragment = null;
    Fragment aboutFragment = null;
    private ProgressDialog pdialog = null;
    private WrapContentDraweeView mAvatar = null;
    private TextView mUsername = null;

    private DrawerLayout mDrawer = null;
    private ActionBarDrawerToggle mToggle = null;

    private UserStatusReceiver mReceiver;
    // press BACK in 2 seconds, app will quit
    private boolean mDoubleBackToExit = false;
    private Handler mHandler = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // how to adjust the height of toolbar
        // http://stackoverflow.com/questions/17439683/how-to-change-action-bar-size
        // zsmth_actionbar_size @ dimen ==> ThemeOverlay.ActionBar @ styles ==> theme @ app_bar_main.xml
//        toolbar.setSubtitle("Hello world");
//        toolbar.setLogo(R.mipmap.zsmth);

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(mToggle);
        mToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // http://stackoverflow.com/questions/33161345/android-support-v23-1-0-update-breaks-navigationview-get-find-header
        View headerView = navigationView.getHeaderView(0);
        mAvatar = (WrapContentDraweeView) headerView.findViewById(R.id.nav_user_avatar);
        mAvatar.setOnClickListener(this);

        mUsername = (TextView) headerView.findViewById(R.id.nav_user_name);
        mUsername.setOnClickListener(this);


        // http://stackoverflow.com/questions/27097126/marquee-title-in-toolbar-actionbar-in-android-with-lollipop-sdk
        TextView titleTextView = null;
        try {
            Field f = toolbar.getClass().getDeclaredField("mTitleTextView");
            f.setAccessible(true);
            titleTextView = (TextView) f.get(toolbar);
            titleTextView.setEllipsize(TextUtils.TruncateAt.START);
        } catch (NoSuchFieldException e) {
        } catch (IllegalAccessException e) {
        }

        // init all fragments
        initFragments();

        FragmentManager fm = getSupportFragmentManager();
        fm.beginTransaction().replace(R.id.content_frame, hotTopicFragment).commit();

        getSupportFragmentManager().addOnBackStackChangedListener(
                new FragmentManager.OnBackStackChangedListener() {
                    public void onBackStackChanged() {
                        //Enable Up button only  if there are entries in the back stack
                        boolean canback = getSupportFragmentManager().getBackStackEntryCount() > 0;
                        if (canback) {
                            mToggle.setDrawerIndicatorEnabled(false);
                            getSupportActionBar().setDisplayShowHomeEnabled(true);
                            getSupportActionBar().setHomeButtonEnabled(true);
                            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                        } else {
                            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
                            mToggle.setDrawerIndicatorEnabled(true);
                            mDrawer.addDrawerListener(mToggle);
                        }
                    }
                });

        // start service to maintain user status
        setupUserStatusReceiver();
        updateUserStatusNow();

        // schedule the periodical run
        MaintainUserStatusService.schedule(MainActivity.this, mReceiver);
    }

    private void updateUserStatusNow() {
        // triger the background service right now
        Intent intent = new Intent(this, MaintainUserStatusService.class);
        intent.putExtra(SMTHApplication.USER_SERVICE_RECEIVER, mReceiver);
        startService(intent);
    }

    private void setupUserStatusReceiver() {
        mReceiver = new UserStatusReceiver(new Handler());
        mReceiver.setReceiver(new UserStatusReceiver.Receiver() {
            @Override
            public void onReceiveResult(int resultCode, Bundle resultData) {
                if (resultCode == RESULT_OK) {
                    Log.d(TAG, "onReceiveResult: " + "to update navigationview" + SMTHApplication.activeUser.toString());
                    UpdateNavigationViewHeader();
                }
            }
        });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
            if (fragment instanceof OnVolumeUpDownListener) {
                OnVolumeUpDownListener frag = (OnVolumeUpDownListener) fragment;
                return frag.onVolumeUpDown(keyCode);
            }
            return false;
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
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    protected void initFragments() {
        hotTopicFragment = new HotTopicFragment();

        // following initilization can be delayed
        favoriteBoardFragment = new FavoriteBoardFragment();
        allBoardFragment = new AllBoardFragment();
        mailListFragment = new MailListFragment();

        settingFragment = new SettingFragment();
        aboutFragment = new LibsBuilder().supportFragment();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == MAIN_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                updateUserStatusNow();
            }
        }
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem login = menu.findItem(R.id.main_action_login);
        MenuItem logout = menu.findItem(R.id.main_action_logout);
        if (SMTHApplication.isValidUser()) {
            login.setVisible(false);
            logout.setVisible(true);
        } else {
            login.setVisible(true);
            logout.setVisible(false);
        }
        return super.onPrepareOptionsMenu(menu);
    }

    // update header view in navigation header
    public void UpdateNavigationViewHeader() {
        // update optionMenu
        getWindow().invalidatePanelMenu(Window.FEATURE_OPTIONS_PANEL);

        if (SMTHApplication.isValidUser()) {
            // update user to login user
            mUsername.setText(SMTHApplication.activeUser.getId());
            mAvatar.setImageFromStringURL(SMTHApplication.activeUser.getFace_url());
        } else {
            // only user to guest
            mUsername.setText(getString(R.string.nav_header_click_to_login));
            mAvatar.setImageResource(R.drawable.ic_person_black_48dp);
        }
    }

    @Override
    public void onBackPressed() {
        if (mDrawer.isDrawerOpen(GravityCompat.START)) {
            mDrawer.closeDrawer(GravityCompat.START);
            return;
        }

        // handle back button for all fragment
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (fragment instanceof FavoriteBoardFragment) {
            if (!favoriteBoardFragment.atFavoriteRoot()) {
                favoriteBoardFragment.popFavoritePath();
                favoriteBoardFragment.RefreshFavoriteBoards();
                return;
            }
        }

        if (fragment != hotTopicFragment) {
            // return to hottopic if we are not there yet
            String title = "首页导读";
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.content_frame, hotTopicFragment).commit();
            setTitle(SMTHApplication.App_Title_Prefix + title);
            return;
        }

        // for other cases, double back to exit app
        DoubleBackToExit();

    }

    private void DoubleBackToExit() {
        if (mDoubleBackToExit) {
            // if mDoubleBackToExit is true, exit now
            quitNow();
        } else {
            // set mDoubleBackToExit = true, and set delayed task to
            // reset it to false
            mDoubleBackToExit = true;
            if (mHandler == null) {
                mHandler = new Handler();
            }
            // reset will be run after 2000 ms
            mHandler.postDelayed(new PendingDoubleBackToExit(), 2000);
            Toast.makeText(this, "再按一次退出zSMTH", Toast.LENGTH_SHORT).show();
        }
    }

    private void quitNow() {
        // stop background service
        MaintainUserStatusService.unschedule(MainActivity.this);

        // quit
        finish();
        android.os.Process.killProcess(android.os.Process.myPid());
        System.exit(0);
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    public void showProgress(String message, final boolean show) {
        if (pdialog == null) {
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
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);

        //noinspection SimplifiableIfStatement
        if (id == R.id.main_action_refresh) {
            if (fragment == hotTopicFragment) {
                hotTopicFragment.RefreshGuidance();
            } else if (fragment == allBoardFragment) {
                allBoardFragment.LoadAllBoardsWithoutCache();
            } else if (fragment == favoriteBoardFragment) {
                favoriteBoardFragment.RefreshFavoriteBoardsWithCache();
            } else {
                Toast toast = Toast.makeText(this, "Refresh", Toast.LENGTH_SHORT);
                toast.show();
            }
            return true;
        } else if (id == R.id.main_action_switch_theme) {
            Toast.makeText(MainActivity.this, "main_action_switch_theme", Toast.LENGTH_SHORT).show();
            return true;
        } else if (id == R.id.main_action_login) {
            onLogin();
            return true;
        } else if (id == R.id.main_action_logout) {
            onLogout();
            return true;
        } else if (id == android.R.id.home) {
            this.onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void onLogin() {
        Intent intent = new Intent(this, LoginActivity.class);
        startActivityForResult(intent, MAIN_ACTIVITY_REQUEST_CODE);
    }

    public void onLogout() {
        Settings.getInstance().setUserOnline(false);
        if (SMTHApplication.activeUser != null) {
            SMTHApplication.activeUser.setId("guest");
        }
        UpdateNavigationViewHeader();

        SMTHHelper helper = SMTHHelper.getInstance();
        helper.wService.logout()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Subscriber<AjaxResponse>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError: " + Log.getStackTraceString(e));
                    }

                    @Override
                    public void onNext(AjaxResponse ajaxResponse) {
                        Log.d(TAG, "onNext: " + ajaxResponse.toString());
                        Toast.makeText(MainActivity.this, ajaxResponse.getAjax_msg(), Toast.LENGTH_SHORT).show();
                    }
                });

    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        Fragment fragment = null;
        String title = "";

        if (id == R.id.nav_guidance) {
            fragment = hotTopicFragment;
            title = "首页导读";
        } else if (id == R.id.nav_favorite) {
            fragment = favoriteBoardFragment;
            title = "收藏夹";
        } else if (id == R.id.nav_all_boards) {
            fragment = allBoardFragment;
            title = "所有版面";
        } else if (id == R.id.nav_mail) {
            fragment = mailListFragment;
            title = "邮件";
        } else if (id == R.id.nav_setting) {
            fragment = settingFragment;
            title = "设置";
            testCodes();
        } else if (id == R.id.nav_about) {
            fragment = aboutFragment;
            title = "关于";
        }

        // switch fragment
        if (fragment != null) {
            FragmentManager fm = getSupportFragmentManager();
            fm.beginTransaction().replace(R.id.content_frame, fragment).commit();
            setTitle(SMTHApplication.App_Title_Prefix + title);
        }

        mDrawer.closeDrawer(GravityCompat.START);
        return true;
    }

    public void testCodes() {
        ImagePipeline imagePipeline = Fresco.getImagePipeline();
        imagePipeline.clearDiskCaches();
    }


    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.nav_user_avatar || id == R.id.nav_user_name) {
            // 点击图标或者文字，都弹出登录对话框或者profiel对话框
            mDrawer.closeDrawer(GravityCompat.START);
            if (SMTHApplication.activeUser != null && !SMTHApplication.activeUser.getId().equals("guest")) {
                Intent intent = new Intent(this, QueryUserActivity.class);
                intent.putExtra(SMTHApplication.QUERY_USER_INFO, SMTHApplication.activeUser.getId());
                startActivity(intent);
            } else {
                onLogin();
            }
        }
    }

    @Override
    public void onTopicFragmentInteraction(Topic item) {
        // will be triggered in HotTopicFragment
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (fragment == hotTopicFragment) {
            if (item.isCategory) return;
            Intent intent = new Intent(this, PostListActivity.class);
            intent.putExtra(SMTHApplication.TOPIC_OBJECT, item);
            intent.putExtra(SMTHApplication.FROM_BOARD, SMTHApplication.FROM_BOARD_HOT);
            startActivity(intent);
        }
    }

    @Override
    public void onListFragmentInteraction(Mail item) {
        // MailListFragment
        Toast.makeText(this, item.toString() + " is clicked", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onBoardFragmentInteraction(Board item) {
        // shared by FavoriteBoard & AllBoard fragment
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if (fragment == favoriteBoardFragment) {
            // favorite fragment, we might enter a folder
            if (item.isFolder()) {
                favoriteBoardFragment.pushFavoritePath(item.getFolderID(), item.getFolderName());
                favoriteBoardFragment.RefreshFavoriteBoards();
            } else {
                startBoardTopicActivity(item);
            }

        } else if (fragment == allBoardFragment) {
            startBoardTopicActivity(item);
        }
    }

    public void startBoardTopicActivity(Board board) {
        Intent intent = new Intent(this, BoardTopicActivity.class);
        intent.putExtra(SMTHApplication.BOARD_OBJECT, (Parcelable) board);
        startActivity(intent);
    }

    class PendingDoubleBackToExit implements Runnable {
        public void run() {
            mDoubleBackToExit = false;
        }
    }

}
