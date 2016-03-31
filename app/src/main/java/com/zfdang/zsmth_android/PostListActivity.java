package com.zfdang.zsmth_android;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.jude.swipbackhelper.SwipeBackHelper;
import com.thefinestartist.finestwebview.FinestWebView;
import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.helpers.RecyclerViewUtil;
import com.zfdang.zsmth_android.models.AlertDialogItem;
import com.zfdang.zsmth_android.models.Board;
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
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private String mFrom;


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

        // define swipe refresh function
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.post_list_swipe_refresh_layout);
        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                reloadPostList();
            }
        });

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
        mFrom = intent.getStringExtra(SMTHApplication.FROM_BOARD);
        // now onCreateOptionsMenu(...) is called again
//        invalidateOptionsMenu();
        Log.d(TAG, String.format("Load post list for topic = %s, source = %s", topic.toString(), mFrom));

        // set onClick Lisetner for page navigator buttons
        findViewById(R.id.post_list_first_page).setOnClickListener(this);
        findViewById(R.id.post_list_pre_page).setOnClickListener(this);
        findViewById(R.id.post_list_next_page).setOnClickListener(this);
        findViewById(R.id.post_list_last_page).setOnClickListener(this);
        findViewById(R.id.post_list_go_page).setOnClickListener(this);

        if(mTopic == null || !mTopic.getTopicID().equals(topic.getTopicID()) || PostListContent.POSTS.size() == 0) {
            // new topic, different topic, or no post loaded
            mTopic = topic;
            reloadPostList();

            setTitle(mTopic.getBoardChsName() + " - 阅读文章");
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
        if(mSwipeRefreshLayout.isRefreshing()) {
            mSwipeRefreshLayout.setRefreshing(false);
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
                            Log.d(TAG, Log.getStackTraceString(e));
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
                        Log.d(TAG, Log.getStackTraceString(e));
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
    public boolean onCreateOptionsMenu(Menu menu) {
        // http://stackoverflow.com/questions/10692755/how-do-i-hide-a-menu-item-in-the-actionbar
        getMenuInflater().inflate(R.menu.post_list_menu, menu);

        MenuItem item = menu.findItem(R.id.post_list_action_enter_board);
        if(SMTHApplication.FROM_BOARD_BOARD.equals(mFrom)) {
            // from BoardTopicActivity
            item.setVisible(false);
        } else if (SMTHApplication.FROM_BOARD_HOT.equals(mFrom)) {
            // from HotTopicFragment
            item.setVisible(true);
        }
        return true;
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
            onBackPressed();
            return true;
        } else if(id == R.id.post_list_action_refresh) {
            reloadPostList();
        } else if(id == R.id.post_list_action_enter_board) {
            Board board = new Board("", mTopic.getBoardChsName(), mTopic.getBoardEngName());
            Intent intent = new Intent(this, BoardTopicActivity.class);
            intent.putExtra("board_object", (Parcelable)board);
            startActivity(intent);
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
                int pageNo;
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
    public void onItemClicked(int position, View v) {

        Log.d(TAG, String.format("Topic information: %s", mTopic.getTopicID()));
        Log.d(TAG, String.format("Post [%s] by %s is clicked", PostListContent.POSTS.get(position).getPostID(), PostListContent.POSTS.get(position).getAuthor()));
    }

    @Override
    public boolean onItemLongClicked(final int position, View v) {
        Log.d(TAG, String.format("Post by %s is long clicked", PostListContent.POSTS.get(position).getAuthor()));


        final AlertDialogItem[] menuItems = {
                new AlertDialogItem(getString(R.string.post_reply_post), R.drawable.ic_reply_black_48dp),       // 0
                new AlertDialogItem(getString(R.string.post_reply_mail), R.drawable.ic_email_black_48dp),    // 1
                new AlertDialogItem(getString(R.string.post_query_author), R.drawable.ic_person_black_48dp),    // 2
                new AlertDialogItem(getString(R.string.post_copy_content), R.drawable.ic_content_copy_black_48dp),    // 3
                new AlertDialogItem(getString(R.string.post_foward_self), R.drawable.ic_send_black_48dp),     // 4
                new AlertDialogItem(getString(R.string.post_foward_external), R.drawable.ic_forward_black_48dp), // 5
                new AlertDialogItem(getString(R.string.post_view_in_browser), R.drawable.ic_open_in_browser_black_48dp), // 6
                new AlertDialogItem(getString(R.string.post_share), R.drawable.ic_share_black_48dp), // 7
                new AlertDialogItem(getString(R.string.post_delete_post), R.drawable.ic_delete_black_48dp),     // 8
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
                        onPostPopupMenuItem(position, which);
                    }
                })
                .show();

        return true;
    }

    private void onPostPopupMenuItem(int position, int which) {
        Log.d(TAG, String.format("MenuItem %d was clicked", which));

        if(which == 0) {
            // post_reply_post

        } else if (which == 1) {
            // post_reply_mail
        } else if (which == 2) {
            // post_query_author
        } else if (which == 3) {
            // copy post content
            // http://stackoverflow.com/questions/8056838/dealing-with-deprecated-android-text-clipboardmanager
            String content;
            Post post = PostListContent.POSTS.get(position);
            if(post != null) {
                content = post.getRawContent();

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
                    final android.content.ClipboardManager clipboardManager = (android.content.ClipboardManager)
                            getSystemService(Context.CLIPBOARD_SERVICE);
                    final android.content.ClipData clipData = android.content.ClipData.newPlainText("PostContent", content);
                    clipboardManager.setPrimaryClip(clipData);
                } else {
                    final android.text.ClipboardManager clipboardManager = (android.text.ClipboardManager)getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboardManager.setText(content);
                }
                Toast.makeText(PostListActivity.this, "帖子内容已复制到剪贴板", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(PostListActivity.this, "复制失败！", Toast.LENGTH_SHORT).show();
            }

        } else if (which == 4) {
            // post_foward_self
        } else if (which == 5) {
            // post_foward_external
            //
        } else if (which == 6) {
            // open post in browser
            String url = String.format("http://m.newsmth.net/article/%s/%s?p=%d", mTopic.getBoardEngName(), mTopic.getTopicID(), mCurrentPageNo);
            new FinestWebView.Builder(this)
                    .statusBarColorRes(R.color.colorPrimaryDark)
                    .toolbarColorRes(R.color.colorPrimary)
                    .titleColorRes(R.color.finestWhite)
                    .titleDefault(String.format("zSMTH - %s", mTopic.getBoardName()))
                    .updateTitleFromHtml(false)
                    .showUrl(false)
                    .showSwipeRefreshLayout(false)
                    .progressBarColorRes(R.color.finestWhite)
                    .progressBarHeight(4)
                    .webViewSupportZoom(true)
                    .show(url);
        } else if (which == 7) {
            // post_share

        } else if (which == 8) {
            // post_delete_post
        }

    }

}
