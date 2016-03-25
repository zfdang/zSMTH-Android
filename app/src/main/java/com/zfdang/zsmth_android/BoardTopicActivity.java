package com.zfdang.zsmth_android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.malinskiy.superrecyclerview.SuperRecyclerView;
import com.zfdang.zsmth_android.listeners.OnTopicFragmentInteractionListener;
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
public class BoardTopicActivity extends AppCompatActivity implements OnTopicFragmentInteractionListener {

    /**
     * Whether or not the activity is in two-pane mode, i.e. running on a tablet
     * device.
     */

    private final String TAG = "BoardTopicActivity";
    private String mBoardChsName = null;
    private String mBoardEngName = null;
    private String mSource = null;

    private ProgressDialog pdialog = null;
    private int mCurrentPageNo = 1;

    private SuperRecyclerView mRecyclerView = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_board_topic);

        Toolbar toolbar = (Toolbar) findViewById(R.id.board_topic_toolbar);
        setSupportActionBar(toolbar);
        toolbar.setTitle(getTitle());

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.board_topic_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        mRecyclerView = (SuperRecyclerView) findViewById(R.id.board_topic_list);
        assert mRecyclerView != null;
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mRecyclerView.setAdapter(new BoardTopicRecyclerViewAdapter(TopicListContent.BOARD_TOPICS, this));

        // get Board information from launcher
        Intent intent = getIntent();
        mBoardChsName = intent.getStringExtra("board_chs_name");
        mSource = intent.getStringExtra("source");
        String engName = intent.getStringExtra("board_eng_name");
        if(engName != mBoardEngName) {
            TopicListContent.clearBoardTopics();
            mBoardEngName = engName;
            TopicListContent.setBoardName(mBoardEngName);
        }

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        updateTitle();

        if(TopicListContent.BOARD_TOPICS.size() == 0) {
            // only load boards on the first time
            RefreshBoardTopics();
        }

    }

    public void updateTitle() {
        String title = "";
//        if(mSource != null && mSource.length() > 0) {
//            title += mSource;
//        }
        if(mBoardChsName != null && mBoardChsName.length() > 0) {
            title += String.format("[%s]%s", mBoardEngName, mBoardChsName);
        } else {
            title += String.format("%s", mBoardEngName);
        }

        setTitle(title);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if(id == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
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

    public void RefreshBoardTopics() {
        showProgress("加载版面文章...", true);
        RefreshBoardTopicsFromMobile();
    }

    public void RefreshBoardTopicsFromMobile() {
        final SMTHHelper helper = SMTHHelper.getInstance();

        helper.mService.getBoardTopicsByPage(mBoardEngName, Integer.toString(mCurrentPageNo))
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

                        TopicListContent.clearBoardTopics();
                        mRecyclerView.getAdapter().notifyDataSetChanged();
                    }

                    @Override
                    public void onCompleted() {
//                        Topic topic = new Topic("-- END --");
//                        TopicListContent.addHotTopic(topic);
//                        mRecyclerView.getAdapter().notifyItemInserted(TopicListContent.HOT_TOPICS.size() - 1);

                        clearLoadingHints();

                        // show finish toast
//                        Toast.makeText(getApplicationContext(), "刷新完成!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, e.toString());
                        clearLoadingHints();

                        Toast.makeText(getApplicationContext(), "获取热帖失败", Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNext(Topic topic) {
                        Log.d(TAG, topic.toString());
                        TopicListContent.addBoardTopic(topic, mBoardEngName);
                        mRecyclerView.getAdapter().notifyItemInserted(TopicListContent.BOARD_TOPICS.size() - 1);
                    }
                });
    }


    public void clearLoadingHints () {
        // disable progress bar
        showProgress("", false);

        // disable SwipeFreshLayout
//        mSwipeRefreshLayout.setRefreshing(false);
    }


    @Override
    public void onTopicFragmentInteraction(Topic item) {
        Intent intent = new Intent(this, PostListActivity.class);
//        intent.putExtra("board_chs_name", item.getBoardChsName());
//        intent.putExtra("board_eng_name", item.getBoardEngName());
//        intent.putExtra("source", item);
        startActivity(intent);
    }
}
