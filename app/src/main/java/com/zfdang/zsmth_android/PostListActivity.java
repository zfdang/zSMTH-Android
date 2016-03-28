package com.zfdang.zsmth_android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.thefinestartist.finestwebview.FinestWebView;
import com.zfdang.zsmth_android.models.AlertDialogItem;
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
        implements View.OnClickListener, PostRecyclerViewAdapter.OnItemClickListener, PostRecyclerViewAdapter.OnItemLongClickListener{

    private static final String TAG = "PostListActivity";
    private RecyclerView mRecyclerView = null;
    private TextView mTitle = null;
    private EditText mPageNo = null;

    public int mCurrentPageNo = 1;

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
        mRecyclerView.setAdapter(new PostRecyclerViewAdapter(PostListContent.POSTS, this));

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
                        String title = String.format("[%d/%d] %s", mCurrentPageNo, mTopic.getTotalPageNo(), mTopic.getTitle());
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
                        mRecyclerView.getAdapter().notifyItemInserted(PostListContent.POSTS.size() - 1);
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

    @Override
    public void onItemClicked(int position, View v) {
        Log.d(TAG, String.format("Post by %s is clicked", PostListContent.POSTS.get(position).getAuthor()));
    }

    @Override
    public boolean onItemLongClicked(int position, View v) {
        Log.d(TAG, String.format("Post by %s is long clicked", PostListContent.POSTS.get(position).getAuthor()));


        final AlertDialogItem[] menuItems = {
                new AlertDialogItem(getString(R.string.post_reply_post), R.drawable.ic_reply_black_48dp),       // 0
                new AlertDialogItem(getString(R.string.post_reply_mail), R.drawable.ic_email_black_48dp),    // 1
                new AlertDialogItem(getString(R.string.post_query_author), R.drawable.ic_person_black_48dp),    // 2
                new AlertDialogItem(getString(R.string.post_copy_content), R.drawable.ic_content_copy_black_48dp),    // 3
                new AlertDialogItem(getString(R.string.post_foward_self), R.drawable.ic_send_black_48dp),     // 4
                new AlertDialogItem(getString(R.string.post_foward_external), R.drawable.ic_forward_black_48dp), // 5
                new AlertDialogItem(getString(R.string.post_view_in_browser), R.drawable.ic_open_in_browser_black_48dp), // 6
                new AlertDialogItem(getString(R.string.post_delete_post), R.drawable.ic_delete_black_48dp),     // 7
        };


        ListAdapter adapter = new ArrayAdapter<AlertDialogItem>(getApplicationContext(), R.layout.post_popup_menu_item, menuItems) {
            ViewHolder holder;
            public View getView(int position, View convertView, ViewGroup parent) {
                final LayoutInflater inflater = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

                if (convertView == null) {
                    convertView = inflater.inflate(R.layout.post_popup_menu_item, null);

                    holder = new ViewHolder();
                    holder.mIcon = (ImageView) convertView.findViewById(R.id.post_popupmenu_icon);
                    holder.mTitle = (TextView) convertView.findViewById(R.id.post_popupmenu_title);
                    convertView.setTag(holder);
                } else {
                    // view already defined, retrieve view holder
                    holder = (ViewHolder) convertView.getTag();
                }

                holder.mTitle.setText(menuItems[position].text);
                holder.mIcon.setImageResource(menuItems[position].icon);
                return convertView;
            }

            class ViewHolder {
                ImageView mIcon;
                TextView mTitle;
            }
        };

        new AlertDialog.Builder(this)
                .setTitle(getString(R.string.post_alert_title))
                .setAdapter(adapter, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        onPostPopupMenuItem(which);
                    }
                })
                .show();

        return true;
    }

    private void onPostPopupMenuItem(int which) {
        Log.d(TAG, String.format("MenuItem %d was clicked", which));

        if(which == 0) {

        } else if (which == 1) {
        } else if (which == 2) {
        } else if (which == 3) {
        } else if (which == 4) {
        } else if (which == 5) {
        } else if (which == 6) {
            new FinestWebView.Builder(this).show("http://www.zfdang.com/");
        } else if (which == 7) {

        }

    }

}
