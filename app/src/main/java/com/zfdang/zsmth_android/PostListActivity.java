package com.zfdang.zsmth_android;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zfdang.zsmth_android.models.Post;
import com.zfdang.zsmth_android.models.PostListContent;
import com.zfdang.zsmth_android.models.Topic;
import com.zfdang.zsmth_android.newsmth.SMTHHelper;

import java.util.List;

import okhttp3.ResponseBody;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * An activity representing a single Topic detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link BoardTopicActivity}.
 */
public class PostListActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "PostListActivity";
    private RecyclerView mRecyclerView = null;
    private TextView mTitle = null;
    private EditText mPageNo = null;

    private int mCurrentPageNo = 1;

    private Topic mTopic = null;

    private ProgressDialog pdialog = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.post_list_toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mTitle = (TextView) findViewById(R.id.post_list_title);
        assert  mTitle != null;
        mPageNo = (EditText) findViewById(R.id.post_list_page_no);
        assert mPageNo != null;

        mRecyclerView = (RecyclerView) findViewById(R.id.post_list);
        assert mRecyclerView != null;
        mRecyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        mRecyclerView.setLayoutManager(linearLayoutManager);
        mRecyclerView.setAdapter(new PostRecyclerViewAdapter(PostListContent.POSTS, null));

        // get Board information from launcher
        Intent intent = getIntent();
        Topic topic = intent.getParcelableExtra("topic_object");
        assert topic != null;
        Log.d(TAG, String.format("Load post list for topic = %s", topic.toString()));

        // set onClick Lisetner for page navigator buttons
        ((Button)findViewById(R.id.post_list_first_page)).setOnClickListener(this);
        ((Button)findViewById(R.id.post_list_pre_page)).setOnClickListener(this);
        ((Button)findViewById(R.id.post_list_next_page)).setOnClickListener(this);
        ((Button)findViewById(R.id.post_list_last_page)).setOnClickListener(this);
        ((Button)findViewById(R.id.post_list_go_page)).setOnClickListener(this);

        if(mTopic == null || !mTopic.getTopicID().equals(topic.getTopicID())) {
            // new topic, different topic, or no post loaded
            mTopic = topic;
            PostListContent.clear();
            updateTitles();
        }

        if(PostListContent.POSTS.size() == 0) {
            showProgress("加载文章中, 请稍候...", true);
            loadPostListByPages();
        }

    }

    private void updateTitles() {
        setTitle(mTopic.getBoardName());
        mTitle.setText(mTopic.getTitle());
    }


    @Override
    protected void onResume() {
        super.onResume();
    }

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
    }

    public void loadPostListByPages () {
        final SMTHHelper helper = SMTHHelper.getInstance();
        helper.wService.getPostListByPage(mTopic.getTopicURL(), mTopic.getTopicID(), mCurrentPageNo)
                .flatMap(new Func1<ResponseBody, Observable<Post>>() {
                    @Override
                    public Observable<Post> call(ResponseBody responseBody) {
                        try {
                            String response = responseBody.string();
                            List<Post> posts = SMTHHelper.ParsePostListFromWWW(response, mTopic);
                            return Observable.from(posts);
                        } catch (Exception e) {
                            Log.d(TAG, e.toString());
                        }
                        return null;
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Post>() {
                    @Override
                    public void onCompleted() {
                        clearLoadingHints();
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, e.toString());
                    }

                    @Override
                    public void onNext(Post post) {
                        PostListContent.addItem(post);
                        mRecyclerView.getAdapter().notifyItemInserted(PostListContent.POSTS.size()-1);
                        Log.d(TAG, post.toString());
                    }
                });

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            // This ID represents the Home or Up button. In the case of this
            // activity, the Up button is shown. Use NavUtils to allow users
            // to navigate up one level in the application structure. For
            // more details, see the Navigation pattern on Android Design:
            //
            // http://developer.android.com/design/patterns/navigation.html#up-vs-back
            //
            NavUtils.navigateUpTo(this, new Intent(this, BoardTopicActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        // page navigation buttons
        switch (v.getId()) {
            case R.id.post_list_first_page:
                break;
            case R.id.post_list_pre_page:
                break;
            case R.id.post_list_next_page:
                break;
            case R.id.post_list_last_page:
                break;
            case R.id.post_list_go_page:
                break;
        }

        Toast.makeText(PostListActivity.this, "Button clicked: " + v.getId(), Toast.LENGTH_SHORT).show();
    }
}
