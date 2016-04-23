package com.zfdang.zsmth_android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import com.jude.swipbackhelper.SwipeBackHelper;
import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.helpers.RecyclerViewUtil;
import com.zfdang.zsmth_android.listeners.EndlessRecyclerOnScrollListener;
import com.zfdang.zsmth_android.listeners.OnTopicFragmentInteractionListener;
import com.zfdang.zsmth_android.models.Board;
import com.zfdang.zsmth_android.models.ComposePostContext;
import com.zfdang.zsmth_android.models.Topic;
import com.zfdang.zsmth_android.models.TopicListContent;
import com.zfdang.zsmth_android.newsmth.SMTHHelper;

import java.util.List;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * An activity representing a list of Topics. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link PostListActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class BoardTopicActivity extends AppCompatActivity
        implements OnTopicFragmentInteractionListener,
        SwipeRefreshLayout.OnRefreshListener
{

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */

    private final String TAG = "BoardTopicActivity";

    private Board mBoard = null;

    private ProgressDialog pdialog = null;
    private int mCurrentPageNo = 1;
    private int LOAD_MORE_THRESHOLD = 1;

    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private EndlessRecyclerOnScrollListener mScrollListener = null;
    private RecyclerView mRecyclerView = null;

    private Settings mSetting;

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

        setContentView(R.layout.activity_board_topic);

        Toolbar toolbar = (Toolbar) findViewById(R.id.board_topic_toolbar);
        setSupportActionBar(toolbar);
        assert toolbar != null;
        toolbar.setTitle(getTitle());

        mSetting = Settings.getInstance();

        // enable pull down to refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        if (mSwipeRefreshLayout == null) throw new AssertionError();
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.board_topic_list);
        assert mRecyclerView != null;
        mRecyclerView.setItemAnimator(new DefaultItemAnimator());
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL, 0));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(new BoardTopicRecyclerViewAdapter(TopicListContent.BOARD_TOPICS, this));

        // enable endless loading
        mScrollListener = new EndlessRecyclerOnScrollListener(linearLayoutManager) {
            @Override
            public void onLoadMore(int current_page) {
                // do something...
                loadMoreItems();
            }
        };
        mRecyclerView.addOnScrollListener(mScrollListener);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // get Board information from launcher
        Intent intent = getIntent();
        Board board = intent.getParcelableExtra(SMTHApplication.BOARD_OBJECT);
        assert board != null;
        if (mBoard == null || !mBoard.getBoardEngName().equals(board.getBoardEngName())) {
            mBoard = board;
            TopicListContent.clearBoardTopics();
            mCurrentPageNo = 1;
        }

        updateTitle();

        if (TopicListContent.BOARD_TOPICS.size() == 0) {
            // only load boards on the first time
            RefreshBoardTopicsWithoutClear();
        }
    }


    public void updateTitle() {
        String title = mBoard.getBoardChsName();
        setTitle(title + " - 主题列表");
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.board_topic_action_sticky) {
            mSetting.toggleShowSticky();
            this.RefreshBoardTopoFromPageOne();
        } else if (id == R.id.board_topic_action_refresh) {
            this.RefreshBoardTopoFromPageOne();
        } else if(id == R.id.board_topic_action_newpost) {
            ComposePostContext postContext = new ComposePostContext();
            postContext.setBoardEngName(mBoard.getBoardEngName());

            Intent intent = new Intent(this, ComposePostActivity.class);
            intent.putExtra(SMTHApplication.COMPOSE_POST_CONTEXT, postContext);
            startActivity(intent);

        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.board_topic_menu, menu);
        return true;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    public void showProgress(String message, final boolean show) {
        if(pdialog == null) {
            pdialog = new ProgressDialog(this);
        }
        if (show) {
            pdialog.setMessage(message);
            pdialog.show();
        } else {
            pdialog.cancel();
        }
    }


    public void clearLoadingHints() {
        // disable progress bar
        if(pdialog != null && pdialog.isShowing()){
            showProgress("", false);
        }

        if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
        }

        if(mScrollListener != null) {
            mScrollListener.setLoading(false);
        }

    }

    // load topics from next page, without alert
    public void loadMoreItems() {
        if(mSwipeRefreshLayout.isRefreshing() || pdialog.isShowing()) {
            return;
        }

        mCurrentPageNo += 1;
        // Log.d(TAG, mCurrentPageNo + " page is loading now...");
        LoadBoardTopicsFromMobile();
    }

    @Override
    public void onRefresh() {
        // this method is slightly different with RefreshBoardTopoFromPageOne
        // this method does not alert since it's triggered by SwipeRefreshLayout
        mCurrentPageNo = 1;
        TopicListContent.clearBoardTopics();
        mRecyclerView.getAdapter().notifyDataSetChanged();
        LoadBoardTopicsFromMobile();
    }


    public void RefreshBoardTopoFromPageOne() {
        showProgress("刷新版面文章...", true);

        TopicListContent.clearBoardTopics();
        mRecyclerView.getAdapter().notifyDataSetChanged();

        mCurrentPageNo = 1;
        LoadBoardTopicsFromMobile();
    }

    public void RefreshBoardTopicsWithoutClear() {
        showProgress("加载版面文章...", true);

        LoadBoardTopicsFromMobile();
    }

    public void LoadBoardTopicsFromMobile() {
        final SMTHHelper helper = SMTHHelper.getInstance();

        helper.mService.getBoardTopicsByPage(mBoard.getBoardEngName(), Integer.toString(mCurrentPageNo))
                .flatMap(new Func1<ResponseBody, Observable<Topic>>() {
                    @Override
                    public Observable<Topic> call(ResponseBody responseBody) {
                        try {
                            String response = responseBody.string();
                            List<Topic> topics = SMTHHelper.ParseBoardTopicsFromMobile(response);
                            return Observable.from(topics);
                        } catch (Exception e) {
                            Log.d(TAG, Log.getStackTraceString(e));
                            return null;
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Topic>() {
                    @Override
                    public void onStart() {
                        super.onStart();

                        Topic topic = new Topic(String.format("第%d页:", mCurrentPageNo));
                        topic.isCategory = true;
                        TopicListContent.addBoardTopic(topic, mBoard.getBoardEngName());
                        mRecyclerView.getAdapter().notifyItemInserted(TopicListContent.BOARD_TOPICS.size() - 1);
                    }

                    @Override
                    public void onCompleted() {
                        clearLoadingHints();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, Log.getStackTraceString(e));

                        clearLoadingHints();

                        Toast.makeText(getApplicationContext(), String.format("获取第%d页的帖子失败", mCurrentPageNo), Toast.LENGTH_LONG).show();
                        mCurrentPageNo -= 1;
                    }

                    @Override
                    public void onNext(Topic topic) {
                        // Log.d(TAG, topic.toString());
                        if(!topic.isSticky || (topic.isSticky && mSetting.isShowSticky())) {
                            TopicListContent.addBoardTopic(topic, mBoard.getBoardEngName());
                            mRecyclerView.getAdapter().notifyItemInserted(TopicListContent.BOARD_TOPICS.size() - 1);
                        }
                    }
                });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            RecyclerViewUtil.ScrollRecyclerViewByKey(mRecyclerView, keyCode);
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
    public void onTopicFragmentInteraction(Topic item) {
        if(item.isCategory) return;
        Intent intent = new Intent(this, PostListActivity.class);
        item.setBoardEngName(mBoard.getBoardEngName());
        item.setBoardChsName(mBoard.getBoardChsName());
        intent.putExtra(SMTHApplication.TOPIC_OBJECT, item);
        intent.putExtra(SMTHApplication.FROM_BOARD, SMTHApplication.FROM_BOARD_BOARD);
        startActivity(intent);
    }


}
