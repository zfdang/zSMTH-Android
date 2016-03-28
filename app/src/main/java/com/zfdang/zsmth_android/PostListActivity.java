package com.zfdang.zsmth_android;

import android.app.ProgressDialog;
import android.content.Context;
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
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zfdang.zsmth_android.listeners.RecyclerItemClickListener;
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
public class PostListActivity extends AppCompatActivity
        implements View.OnClickListener {

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

        mRecyclerView.addOnItemTouchListener(
                new RecyclerItemClickListener(getApplicationContext(), mRecyclerView, new RecyclerItemClickListener.OnItemClickListener() {
                    @Override
                    public void onItemClick(View view, int position) {
                        // do whatever
                        Log.d(TAG, String.format("Post by %s was clicked", PostListContent.POSTS.get(position).getAuthor()));
                    };

                    @Override
                    public void onItemLongClick(View view, int position) {
                        // ...
                        Log.d(TAG, String.format("Post by %s was long clicked", PostListContent.POSTS.get(position).getAuthor()));
                    };
                }));

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

        if(mTopic == null || !mTopic.getTopicID().equals(topic.getTopicID()) || PostListContent.POSTS.size() == 0) {
            // new topic, different topic, or no post loaded
            mTopic = topic;
            reloadPostList();

            setTitle(mTopic.getBoardName());
        }
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

    public void reloadPostList() {
        showProgress("加载文章中, 请稍候...", true);

        PostListContent.clear();
        mRecyclerView.getAdapter().notifyDataSetChanged();

        loadPostListByPages();
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
                        String title = String.format("[%d/%d]%s", mCurrentPageNo, mTopic.getTotalPageNo(), mTopic.getTitle());
                        mTitle.setText(title);
                        mPageNo.setText(String.format("%d", mCurrentPageNo));

                        clearLoadingHints();
                    }

                    @Override
                    public void onError(Throwable e) {
                        clearLoadingHints();
                        Log.d(TAG, e.toString());
                        Toast.makeText(PostListActivity.this, "加载失败！", Toast.LENGTH_SHORT).show();
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
                if(mCurrentPageNo == 1) {
                    Toast.makeText(PostListActivity.this, "已在首页！", Toast.LENGTH_SHORT).show();
                } else {
                    mCurrentPageNo = 1;
                    reloadPostList();
                }
                break;
            case R.id.post_list_pre_page:
                if(mCurrentPageNo == 1) {
                    Toast.makeText(PostListActivity.this, "已在首页！", Toast.LENGTH_SHORT).show();
                } else {
                    mCurrentPageNo -= 1;
                    reloadPostList();
                }
                break;
            case R.id.post_list_next_page:
                if(mCurrentPageNo == mTopic.getTotalPageNo()) {
                    Toast.makeText(PostListActivity.this, "已在末页！", Toast.LENGTH_SHORT).show();
                } else {
                    mCurrentPageNo += 1;
                    reloadPostList();
                }
                break;
            case R.id.post_list_last_page:
                if(mCurrentPageNo == mTopic.getTotalPageNo()) {
                    Toast.makeText(PostListActivity.this, "已在末页！", Toast.LENGTH_SHORT).show();
                } else {
                    mCurrentPageNo = mTopic.getTotalPageNo();
                    reloadPostList();
                }
                break;
            case R.id.post_list_go_page:
                int pageNo = 1;
                try{
                    pageNo = Integer.parseInt(mPageNo.getText().toString());
                    if(mCurrentPageNo == pageNo) {
                        Toast.makeText(PostListActivity.this, String.format("已在第%d页！", pageNo), Toast.LENGTH_SHORT).show();
                    } else if (pageNo >= 1 && pageNo <= mTopic.getTotalPageNo()){
                        mCurrentPageNo = pageNo;
                        // turn off keyboard
                        mPageNo.clearFocus();
                        InputMethodManager im = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
                        im.hideSoftInputFromWindow(mPageNo.getWindowToken(), 0);
                        // jump now
                        reloadPostList();
                    } else {
                        Toast.makeText(PostListActivity.this, "非法页码！", Toast.LENGTH_SHORT).show();
                    }
                } catch(Exception e) {
                    Toast.makeText(PostListActivity.this, "非法输入！", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }
}
