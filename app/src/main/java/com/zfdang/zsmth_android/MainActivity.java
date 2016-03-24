package com.zfdang.zsmth_android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.umeng.analytics.MobclickAgent;
import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.listeners.OnBoardFragmentInteractionListener;
import com.zfdang.zsmth_android.listeners.OnTopicFragmentInteractionListener;
import com.zfdang.zsmth_android.models.Board;
import com.zfdang.zsmth_android.models.Mail;
import com.zfdang.zsmth_android.models.Post;
import com.zfdang.zsmth_android.models.Topic;

import java.lang.reflect.Field;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        View.OnClickListener,
        OnTopicFragmentInteractionListener,
        OnBoardFragmentInteractionListener,
        MailListFragment.OnListFragmentInteractionListener,
        PostListFragment.OnListFragmentInteractionListener
//        SettingFragment.OnFragmentInteractionListener,
//        AboutFragment.OnFragmentInteractionListener
{

    // guidance fragment: display hot topics
    // this fragment is using RecyclerView to show all hot topics
    HotTopicFragment hotTopicFragment = null;
    FavoriteBoardFragment favoriteBoardFragment = null;
    AllBoardFragment allBoardFragment = null;
    PostListFragment postListFragment = null;
    MailListFragment mailListFragment = null;

    SettingFragment settingFragment = null;
    AboutFragment aboutFragment = null;

    private ProgressDialog pdialog = null;
    // used by startActivityForResult
    static final int MAIN_ACTIVITY_REQUEST_CODE = 9527;  // The request code

    private ImageView mAvatar = null;
    private TextView mUsername = null;

    private DrawerLayout mDrawer = null;
    private  ActionBarDrawerToggle mToggle = null;

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

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mDrawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        mToggle = new ActionBarDrawerToggle(
                this, mDrawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        mDrawer.addDrawerListener(mToggle);
        mToggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        // http://stackoverflow.com/questions/33161345/android-support-v23-1-0-update-breaks-navigationview-get-find-header
        View headerView = navigationView.getHeaderView(0);
        mAvatar = (ImageView) headerView.findViewById(R.id.nav_user_avatar);
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
//                        boolean canback = getSupportFragmentManager().getBackStackEntryCount()>0;
//                        mToggle.setDrawerIndicatorEnabled(!canback);
//                        getSupportActionBar().setDisplayHomeAsUpEnabled(canback);
                    }
                });
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
        postListFragment = new PostListFragment();

        settingFragment = new SettingFragment();
        aboutFragment = new AboutFragment();
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == MAIN_ACTIVITY_REQUEST_CODE) {
            if (resultCode == RESULT_OK) {
                // get username from data, then update mUsername
                String username = data.getStringExtra("username");
                if(username != null && username.length() > 0){
                    // update displayed user name
                    mUsername.setText(username);

                    // TODO: update user_avatar
                }
            }
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
        } else if (fragment instanceof PostListFragment) {
            super.onBackPressed();
            return;
        }
        // for other cases, double back to exit app
        DoubleBackToExit();

    }


    // press BACK in 2 seconds, app will quit
    private boolean mDoubleBackToExit = false;
    private Handler mHandler = null;

    class PendingDoubleBackToExit implements Runnable {
        public void run() {
            mDoubleBackToExit = false;
        }
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
//        finish();
//        android.os.Process.killProcess(android.os.Process.myPid());
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
        if (id == R.id.action_refresh) {
            if (fragment == allBoardFragment) {
                allBoardFragment.LoadAllBoardsWithoutCache();
            } else {
                Toast toast = Toast.makeText(this, "Refresh", Toast.LENGTH_SHORT);
                toast.show();
            }
            return true;
        } else if (id == R.id.action_switch_theme) {

        } else if (id == R.id.action_login) {

        } else if (id == R.id.action_logout) {

        }

        return super.onOptionsItemSelected(item);
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

    @Override
    public void onClick(View v) {
        int id = v.getId();
        if (id == R.id.nav_user_avatar || id == R.id.nav_user_name) {
            // 点击图标或者文字，都弹出登录对话框
            mDrawer.closeDrawer(GravityCompat.START);
            Intent intent = new Intent(this, LoginActivity.class);
            startActivityForResult(intent, MAIN_ACTIVITY_REQUEST_CODE);
        }
    }

    @Override
    public void onTopicFragmentInteraction(Topic item) {
        // shared by HotTopicFragment or BoardTopicFragment
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.content_frame);
        if(fragment == hotTopicFragment) {
            // switch fragment
            FragmentManager fm = getSupportFragmentManager();
            mToggle.setDrawerIndicatorEnabled(false);
            fm.beginTransaction()
                    .replace(R.id.content_frame, postListFragment)
                    .addToBackStack(null)
                    .commit();
            setTitle(SMTHApplication.App_Title_Prefix + item.getBoardName());

        } else {
            Log.d("MainActivity", item.getTitle() + "is clicked");
            Toast.makeText(this, item.getTitle() + " is clicked", Toast.LENGTH_LONG).show();
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
        if(fragment == favoriteBoardFragment) {
            if(item.isFolder()) {
                favoriteBoardFragment.pushFavoritePath(item.getFolderID(), item.getFolderName());
                favoriteBoardFragment.RefreshFavoriteBoards();
            } else {
                Toast.makeText(this, item.toString() + " is clicked", Toast.LENGTH_LONG).show();
            }

        } else if(fragment == allBoardFragment) {
            Toast.makeText(this, item.toString() + " is clicked", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public void onListFragmentInteraction(Post item) {
        // PostListFragment
        Toast.makeText(this, item.toString() + " is clicked", Toast.LENGTH_LONG).show();
    }

}
