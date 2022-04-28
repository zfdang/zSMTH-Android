package com.zfdang.zsmth_android;

import android.content.Intent;
import android.os.Bundle;

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import androidx.appcompat.app.ActionBar;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.util.Log;
import android.view.Gravity;
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
import com.zfdang.zsmth_android.newsmth.AjaxResponse;
import com.zfdang.zsmth_android.newsmth.SMTHHelper;

import io.reactivex.ObservableSource;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;

import java.util.List;

import okhttp3.ResponseBody;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

/**
 * An activity representing a list of Topics. This activity
 * has different presentations for handset and tablet-size devices. On
 * handsets, the activity presents a list of items, which when touched,
 * lead to a {@link PostListActivity} representing
 * item details. On tablets, the activity presents the list of items and
 * item details side-by-side using two vertical panes.
 */
public class BoardTopicActivity extends SMTHBaseActivity
        implements OnTopicFragmentInteractionListener, SwipeRefreshLayout.OnRefreshListener, PopupSearchWindow.SearchInterface {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */

    private final String TAG = "BoardTopicActivity";

    private Board mBoard = null;

    private int mCurrentPageNo = 1;
    private int LOAD_MORE_THRESHOLD = 1;

    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private EndlessRecyclerOnScrollListener mScrollListener = null;
    private RecyclerView mRecyclerView = null;

    private Settings mSetting;

    private boolean isSearchMode = false;

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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == ComposePostActivity.COMPOSE_ACTIVITY_REQUEST_CODE) {
            // returned from compose activity
            // ideally, we should also check the resultCode
            RefreshBoardTopicFromPageOne();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onBackPressed() {
        if (isSearchMode) {
            onRefresh();
        } else {
            super.onBackPressed();
        }
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
        LinearLayoutManager linearLayoutManager = new WrapContentLinearLayoutManager(this);
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
            this.RefreshBoardTopicFromPageOne();
        } else if (id == R.id.board_topic_action_refresh) {
            this.RefreshBoardTopicFromPageOne();
        } else if (id == R.id.board_topic_action_newpost) {
            ComposePostContext postContext = new ComposePostContext();
            postContext.setBoardEngName(mBoard.getBoardEngName());
            postContext.setComposingMode(ComposePostContext.MODE_NEW_POST);

            Intent intent = new Intent(this, ComposePostActivity.class);
            intent.putExtra(SMTHApplication.COMPOSE_POST_CONTEXT, postContext);
            startActivityForResult(intent, ComposePostActivity.COMPOSE_ACTIVITY_REQUEST_CODE);
        } else if (id == R.id.board_topic_action_search) {
            PopupSearchWindow popup = new PopupSearchWindow();
            popup.initPopupWindow(this);
            popup.showAtLocation(mRecyclerView, Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 0);
        } else if (id == R.id.board_topic_action_favorite) {
            SMTHHelper helper = SMTHHelper.getInstance();
            helper.wService.manageFavoriteBoard("0", "ab", this.mBoard.getBoardEngName())
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Observer<AjaxResponse>() {
                        @Override
                        public void onSubscribe(@NonNull Disposable disposable) {

                        }

                        @Override
                        public void onNext(@NonNull AjaxResponse ajaxResponse) {
                            Log.d(TAG, "onNext: " + ajaxResponse.toString());
                            if (ajaxResponse.getAjax_st() == AjaxResponse.AJAX_RESULT_OK) {
                                Toast.makeText(BoardTopicActivity.this, ajaxResponse.getAjax_msg() + "\n请手动刷新收藏夹！", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(BoardTopicActivity.this, ajaxResponse.toString(), Toast.LENGTH_LONG).show();
                            }

                        }

                        @Override
                        public void onError(@NonNull Throwable e) {
                            Toast.makeText(BoardTopicActivity.this, "收藏版面失败！\n" + e.toString(), Toast.LENGTH_LONG).show();

                        }

                        @Override
                        public void onComplete() {

                        }
                    });
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.board_topic_menu, menu);
        return true;
    }

    public void clearLoadingHints() {
        dismissProgress();

        if (mSwipeRefreshLayout != null && mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);  // This hides the spinner
        }

        if (mScrollListener != null) {
            mScrollListener.setLoading(false);
        }
    }

    // load topics from next page, without alert
    public void loadMoreItems() {
        if (isSearchMode || mSwipeRefreshLayout.isRefreshing() || pDialog.isShowing()) {
            return;
        }

        mCurrentPageNo += 1;
        // Log.d(TAG, mCurrentPageNo + " page is loading now...");
        LoadBoardTopics();
    }

    @Override
    public void onRefresh() {
        // this method is slightly different with RefreshBoardTopicFromPageOne
        // this method does not alert since it's triggered by SwipeRefreshLayout
        mCurrentPageNo = 1;
        TopicListContent.clearBoardTopics();
        mRecyclerView.getAdapter().notifyDataSetChanged();
        LoadBoardTopics();
    }

    public void RefreshBoardTopicFromPageOne() {
        showProgress("刷新版面文章...");

        TopicListContent.clearBoardTopics();
        mRecyclerView.getAdapter().notifyDataSetChanged();

        mCurrentPageNo = 1;
        LoadBoardTopics();
    }

    public void RefreshBoardTopicsWithoutClear() {
        showProgress("加载版面文章...");

        LoadBoardTopics();
    }

    public void LoadBoardTopics() {

        isSearchMode = false;
        final SMTHHelper helper = SMTHHelper.getInstance();

        helper.wService.getBoardTopicsByPage(mBoard.getBoardEngName(), Integer.toString(mCurrentPageNo))
                .flatMap(new Function<ResponseBody, ObservableSource<Topic>>() {
                    @Override
                    public ObservableSource<Topic> apply(@NonNull ResponseBody responseBody) throws Exception {
                        try {
                            String response = responseBody.string();
                            List<Topic> topics = SMTHHelper.ParseBoardTopicsFromWWW(response);
                            // add page separator
                            Topic separator = new Topic(String.format("第 %d 页", mCurrentPageNo));
                            separator.isCategory = true;
                            topics.add(0, separator);
                            // return
                            return Observable.fromIterable(topics);
                        } catch (Exception e) {
                            Log.e(TAG, "call: " + Log.getStackTraceString(e));
                            return null;
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Topic>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable disposable) {
                    }

                    @Override
                    public void onNext(@NonNull Topic topic) {
                        // Log.d(TAG, topic.toString());
                        if (!topic.isSticky || mSetting.isShowSticky()) {
                            TopicListContent.addBoardTopic(topic, mBoard.getBoardEngName());
                            mRecyclerView.post(new Runnable() {
                                public void run() {
                                    // There is no need to use notifyDataSetChanged()
                                    mRecyclerView.getAdapter().notifyItemInserted(TopicListContent.BOARD_TOPICS.size() - 1);
                                }
                            });
                        }
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        clearLoadingHints();

                        Toast.makeText(SMTHApplication.getAppContext(), String.format("获取第%d页的帖子失败!\n", mCurrentPageNo) + e.toString(),
                                Toast.LENGTH_LONG).show();
                        mCurrentPageNo -= 1;
                    }

                    @Override
                    public void onComplete() {
                        clearLoadingHints();
                    }
                });
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (Settings.getInstance().isVolumeKeyScroll() && (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            RecyclerViewUtil.ScrollRecyclerViewByKey(mRecyclerView, keyCode);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    // http://stackoverflow.com/questions/4500354/control-volume-keys
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {
        // disable the beep sound when volume up/down is pressed
        if (Settings.getInstance().isVolumeKeyScroll() && (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
            return true;
        }
        return super.onKeyUp(keyCode, event);
    }

    @Override
    public void onTopicFragmentInteraction(Topic item) {
        if (item.isCategory) return;
        Intent intent = new Intent(this, PostListActivity.class);
        item.setBoardEngName(mBoard.getBoardEngName());
        item.setBoardChsName(mBoard.getBoardChsName());
        intent.putExtra(SMTHApplication.TOPIC_OBJECT, item);
        intent.putExtra(SMTHApplication.FROM_BOARD, SMTHApplication.FROM_BOARD_BOARD);
        startActivity(intent);
    }

    @Override
    public void OnSearchAction(String keyword, String author, boolean elite, boolean attachment) {
        Log.d(TAG, "OnSearchAction: " + keyword + author + elite + attachment);

        isSearchMode = true;
        showProgress("加载搜索结果...");

        TopicListContent.BOARD_TOPICS.clear();
        mRecyclerView.getAdapter().notifyDataSetChanged();

        String eliteStr = null;
        if (elite) eliteStr = "on";

        String attachmentStr = null;
        if (attachment) attachmentStr = "on";

        SMTHHelper helper = SMTHHelper.getInstance();
        helper.wService.searchTopicInBoard(keyword, author, eliteStr, attachmentStr, this.mBoard.getBoardEngName())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .flatMap(new Function<ResponseBody, ObservableSource<Topic>>() {
                    @Override
                    public ObservableSource<Topic> apply(@NonNull ResponseBody responseBody) throws Exception {
                        try {
                            String response = responseBody.string();
                            List<Topic> topics = SMTHHelper.ParseSearchResultFromWWW(response);
                            Topic topic = new Topic("搜索模式 - 下拉或按返回键退出搜索模式");
                            topics.add(0, topic);
                            return Observable.fromIterable(topics);
                        } catch (Exception e) {
                            Log.d(TAG, Log.getStackTraceString(e));
                            return null;
                        }
                    }
                })
                .subscribe(new Observer<Topic>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable disposable) {

                    }

                    @Override
                    public void onNext(@NonNull Topic topic) {
                        TopicListContent.addBoardTopic(topic, mBoard.getBoardEngName());
                        mRecyclerView.getAdapter().notifyItemInserted(TopicListContent.BOARD_TOPICS.size() - 1);

                    }

                    @Override
                    public void onError(@NonNull Throwable e) {
                        Toast.makeText(SMTHApplication.getAppContext(), "加载搜索结果失败!\n" + e.toString(), Toast.LENGTH_LONG).show();

                    }

                    @Override
                    public void onComplete() {
                        dismissProgress();
                    }
                });
    }
}
