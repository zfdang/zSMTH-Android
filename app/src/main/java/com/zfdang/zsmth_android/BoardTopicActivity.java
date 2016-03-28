package com.zfdang.zsmth_android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.zfdang.zsmth_android.listeners.EndlessRecyclerOnScrollListener;
import com.zfdang.zsmth_android.listeners.OnTopicFragmentInteractionListener;
import com.zfdang.zsmth_android.models.Board;
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
    private String mSource = null;

    private ProgressDialog pdialog = null;
    private int mCurrentPageNo = 1;
    private int LOAD_MORE_THRESHOLD = 1;

    private SwipeRefreshLayout mSwipeRefreshLayout = null;
    private EndlessRecyclerOnScrollListener mScrollListener = null;
    private RecyclerView mRecyclerView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_topic);

        Toolbar toolbar = (Toolbar) findViewById(R.id.board_topic_toolbar);
        setSupportActionBar(toolbar);
        assert toolbar != null;
        toolbar.setTitle(getTitle());


        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.board_topic_fab);
        assert fab != null;
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // enable pull down to refresh
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setOnRefreshListener(this);

        mRecyclerView = (RecyclerView) findViewById(R.id.board_topic_list);
        assert mRecyclerView != null;
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
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
        Board board = intent.getParcelableExtra("board_object");
        assert board != null;
        mSource = intent.getStringExtra("source");
        if (mBoard == null || !mBoard.getBoardEngName().equals(board.getBoardEngName())) {
            mBoard = board;
            TopicListContent.clearBoardTopics();
            mCurrentPageNo = 1;
        }

        updateTitle();

        if (TopicListContent.BOARD_TOPICS.size() == 0) {
            // only load boards on the first time
            RefreshBoardTopics();
        }
    }

    public void updateTitle() {
        String title = mBoard.getBoardName();
        setTitle(title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
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
    public void RefreshBoardTopics() {
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
                            Log.d(TAG, e.toString());
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
                        TopicListContent.addBoardTopic(topic, mBoard.getBoardEngName());
                        mRecyclerView.getAdapter().notifyItemInserted(TopicListContent.BOARD_TOPICS.size() - 1);
                    }

                    @Override
                    public void onCompleted() {
                        clearLoadingHints();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, e.toString());
                        clearLoadingHints();

                        Toast.makeText(getApplicationContext(), String.format("获取第%d页的帖子失败", mCurrentPageNo), Toast.LENGTH_LONG).show();
                        mCurrentPageNo -= 1;
                    }

                    @Override
                    public void onNext(Topic topic) {
                        // Log.d(TAG, topic.toString());
                        TopicListContent.addBoardTopic(topic, mBoard.getBoardEngName());
                        mRecyclerView.getAdapter().notifyItemInserted(TopicListContent.BOARD_TOPICS.size() - 1);
                    }
                });
    }




    @Override
    public void onTopicFragmentInteraction(Topic item) {
        Intent intent = new Intent(this, PostListActivity.class);
        item.setBoardEngName(mBoard.getBoardEngName());
        item.setBoardChsName(mBoard.getBoardChsName());
        intent.putExtra("topic_object", item);
        startActivity(intent);
    }

    // load more topics
    public void loadMoreItems() {
        mCurrentPageNo += 1;
        Log.d(TAG, mCurrentPageNo + " page is loading now...");
        LoadBoardTopicsFromMobile();
    }

    @Override
    public void onRefresh() {
        mCurrentPageNo = 1;
        TopicListContent.clearBoardTopics();
        mRecyclerView.getAdapter().notifyDataSetChanged();
        LoadBoardTopicsFromMobile();
    }
}
