package com.zfdang.zsmth_android;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Parcelable;
import android.text.TextUtils;
import android.util.Log;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.jude.swipbackhelper.SwipeBackHelper;
import com.scwang.smart.refresh.footer.ClassicsFooter;
import com.scwang.smart.refresh.header.ClassicsHeader;
import com.scwang.smart.refresh.layout.SmartRefreshLayout;
import com.scwang.smart.refresh.layout.api.RefreshLayout;
import com.scwang.smart.refresh.layout.listener.OnLoadMoreListener;
import com.scwang.smart.refresh.layout.listener.OnRefreshListener;
import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.helpers.RecyclerViewUtil;
import com.zfdang.zsmth_android.models.Attachment;
import com.zfdang.zsmth_android.models.Board;
import com.zfdang.zsmth_android.models.ComposePostContext;
import com.zfdang.zsmth_android.models.Post;
import com.zfdang.zsmth_android.models.PostActionAlertDialogItem;
import com.zfdang.zsmth_android.models.PostListContent;
import com.zfdang.zsmth_android.models.Topic;
import com.zfdang.zsmth_android.newsmth.AjaxResponse;
import com.zfdang.zsmth_android.newsmth.SMTHHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import cn.sharesdk.framework.Platform;
import cn.sharesdk.framework.PlatformActionListener;
import cn.sharesdk.onekeyshare.OnekeyShare;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import pub.devrel.easypermissions.AfterPermissionGranted;
import pub.devrel.easypermissions.EasyPermissions;

/**
 * An activity representing a single Topic detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link BoardTopicActivity}.
 */
public class PostListActivity extends SMTHBaseActivity
    implements View.OnClickListener, OnTouchListener, RecyclerViewGestureListener.OnItemLongClickListener, PopupLikeWindow.OnLikeInterface,
    PopupForwardWindow.OnForwardInterface, PopupBanWindow.OnBanIDInterface {

  private static final String TAG = "PostListActivity";
  public RecyclerView mRecyclerView = null;
  private TextView mTitle = null;
  private EditText mPageNo = null;

  public int mCurrentPageNo = 1;
  private String mFilterUser = null;

  private Topic mTopic = null;

  static {
    ClassicsHeader.REFRESH_HEADER_PULLING = "下拉可以刷新";
    ClassicsHeader.REFRESH_HEADER_REFRESHING = "正在刷新...";
    ClassicsHeader.REFRESH_HEADER_LOADING = "正在加载...";
    ClassicsHeader.REFRESH_HEADER_RELEASE = "释放立即刷新";
    ClassicsHeader.REFRESH_HEADER_FINISH = "刷新完成";
    ClassicsHeader.REFRESH_HEADER_FAILED = "刷新失败";
    ClassicsHeader.REFRESH_HEADER_UPDATE = "上次更新 M-d HH:mm";

    ClassicsFooter.REFRESH_FOOTER_PULLING = "上拉可以翻页";
    ClassicsFooter.REFRESH_FOOTER_RELEASE = "释放立即翻页";
    ClassicsFooter.REFRESH_FOOTER_LOADING = "正在加载下一页...";
    ClassicsFooter.REFRESH_FOOTER_REFRESHING = "正在刷新...";
    ClassicsFooter.REFRESH_FOOTER_FINISH = "";
    ClassicsFooter.REFRESH_FOOTER_FAILED = "";
    ClassicsFooter.REFRESH_FOOTER_NOTHING = "";
  }


  private SmartRefreshLayout mRefreshLayout;
  private String mFrom;

  private GestureDetector mGestureDetector;
  private LinearLayoutManager linearLayoutManager;

  // used for captureViewInternal
  View capView1;
  View capView2;
  String capPostID;
  private final static int RC_READ_WRITE_STORAGE = 1245;

  @Override protected void onDestroy() {
    super.onDestroy();
    SwipeBackHelper.onDestroy(this);
  }

  @Override
  public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
    super.onRequestPermissionsResult(requestCode, permissions, grantResults);

    // Forward results to EasyPermissions
    EasyPermissions.onRequestPermissionsResult(requestCode, permissions, grantResults, this);
  }

  @Override protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    SwipeBackHelper.onPostCreate(this);
  }

  @Override protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    if(requestCode == ComposePostActivity.COMPOSE_ACTIVITY_REQUEST_CODE) {
      // returned from Compose activity, refresh current post
      // TODO: check resultCode
      reloadPostList();
    }
    super.onActivityResult(requestCode, resultCode, data);
  }

  @Override protected void onCreate(Bundle savedInstanceState) {
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
    assert mTitle != null;
    mPageNo = (EditText) findViewById(R.id.post_list_page_no);
    assert mPageNo != null;

    // define swipe refresh function
    mRefreshLayout = (SmartRefreshLayout) findViewById(R.id.post_list_swipe_refresh_layout);
    mRefreshLayout.setEnableAutoLoadMore(false);
    mRefreshLayout.setEnableScrollContentWhenLoaded(false);
    mRefreshLayout.setEnableOverScrollBounce(false);
    mRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
      @Override public void onRefresh(RefreshLayout refreshLayout) {
        // reload current page
        reloadPostListWithoutAlert();
      }
    });
    mRefreshLayout.setOnLoadMoreListener(new OnLoadMoreListener() {
      @Override public void onLoadMore(RefreshLayout refreshLayout) {
        // load next page if available
        goToNextPage();
      }
    });

    mRecyclerView = (RecyclerView) findViewById(R.id.post_list);
    assert mRecyclerView != null;
    mRecyclerView.addItemDecoration(
        new DividerItemDecoration(this, LinearLayoutManager.VERTICAL, R.drawable.recyclerview_divider));
    linearLayoutManager = new WrapContentLinearLayoutManager(this);
    mRecyclerView.setLayoutManager(linearLayoutManager);
    mRecyclerView.setAdapter(new PostRecyclerViewAdapter(PostListContent.POSTS, this));

    //  holder.mView.setOnTouchListener(this); so the event will be sent from holder.mView
    mGestureDetector = new GestureDetector(SMTHApplication.getAppContext(), new RecyclerViewGestureListener(this, mRecyclerView));

    // get Board information from launcher
    Intent intent = getIntent();
    Topic topic = intent.getParcelableExtra(SMTHApplication.TOPIC_OBJECT);
    assert topic != null;
    mFrom = intent.getStringExtra(SMTHApplication.FROM_BOARD);
    // now onCreateOptionsMenu(...) is called again
    //        invalidateOptionsMenu();
    //        Log.d(TAG, String.format("Load post list for topic = %s, source = %s", topic.toString(), mFrom));

    // set onClick Lisetner for page navigator buttons
    findViewById(R.id.post_list_first_page).setOnClickListener(this);
    findViewById(R.id.post_list_pre_page).setOnClickListener(this);
    findViewById(R.id.post_list_next_page).setOnClickListener(this);
    findViewById(R.id.post_list_last_page).setOnClickListener(this);
    findViewById(R.id.post_list_go_page).setOnClickListener(this);

    LinearLayout navLayout = (LinearLayout) findViewById(R.id.post_list_action_layout);
    if (Settings.getInstance().hasPostNavBar()) {
      navLayout.setVisibility(View.VISIBLE);
    } else {
      navLayout.setVisibility(View.GONE);
    }
    initPostNavigationButtons();

    if (mTopic == null || !mTopic.getTopicID().equals(topic.getTopicID()) || PostListContent.POSTS.size() == 0) {
      // new topic, different topic, or no post loaded
      mTopic = topic;
      mFilterUser = null;
      reloadPostList();

      setTitle(mTopic.getBoardChsName() + " - 阅读文章");
    }
  }

  public void initPostNavigationButtons() {
    int alphaValue = 50;

    ImageButton imageButton;
    imageButton = (ImageButton) findViewById(R.id.post_list_action_top);
    imageButton.setAlpha(alphaValue);
    imageButton.setOnClickListener(this);

    imageButton = (ImageButton) findViewById(R.id.post_list_action_up);
    imageButton.setAlpha(alphaValue);
    imageButton.setOnClickListener(this);

    imageButton = (ImageButton) findViewById(R.id.post_list_action_down);
    imageButton.setAlpha(alphaValue);
    imageButton.setOnClickListener(this);

    imageButton = (ImageButton) findViewById(R.id.post_list_action_bottom);
    imageButton.setAlpha(alphaValue);
    imageButton.setOnClickListener(this);
  }

  public void clearLoadingHints() {
    dismissProgress();

    if (mRefreshLayout.isRefreshing()) {
      mRefreshLayout.finishRefresh(100);
    }
    if (mRefreshLayout.isLoading()) {
      mRefreshLayout.finishLoadMore(100);
    }
  }

  public void reloadPostListWithoutAlert() {
    PostListContent.clear();
    mRecyclerView.getAdapter().notifyDataSetChanged();

    loadPostListByPages();
  }

  public void reloadPostList() {
    showProgress("加载文章中, 请稍候...");

    reloadPostListWithoutAlert();
  }

  public void loadPostListByPages() {
    final SMTHHelper helper = SMTHHelper.getInstance();
    helper.wService.getPostListByPage(mTopic.getTopicURL(), mTopic.getTopicID(), mCurrentPageNo, mFilterUser)
        .flatMap(new Function<ResponseBody, Observable<Post>>() {
          @Override public Observable<Post> apply(@NonNull ResponseBody responseBody) throws Exception {
            try {
              String response = responseBody.string();
              List<Post> posts = SMTHHelper.ParsePostListFromWWW(response, mTopic);
              return Observable.fromIterable(posts);
            } catch (Exception e) {
              Log.e(TAG, Log.getStackTraceString(e));
            }
            return null;
          }
        })
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<Post>() {
          @Override public void onSubscribe(@NonNull Disposable disposable) {

          }

          @Override public void onNext(@NonNull Post post) {
            // Log.d(TAG, post.toString());
            PostListContent.addItem(post);
            mRecyclerView.getAdapter().notifyItemInserted(PostListContent.POSTS.size() - 1);
          }

          @Override public void onError(@NonNull Throwable e) {
            clearLoadingHints();
            Toast.makeText(SMTHApplication.getAppContext(), "加载失败！\n" + e.toString(), Toast.LENGTH_LONG).show();
          }

          @Override public void onComplete() {
            String title = String.format("[%d/%d] %s", mCurrentPageNo, mTopic.getTotalPageNo(), mTopic.getTitle());
            mTitle.setText(title);
            mPageNo.setText(String.format("%d", mCurrentPageNo));

            clearLoadingHints();
          }
        });
  }

  @Override public boolean onCreateOptionsMenu(Menu menu) {
    // http://stackoverflow.com/questions/10692755/how-do-i-hide-a-menu-item-in-the-actionbar
    getMenuInflater().inflate(R.menu.post_list_menu, menu);

    MenuItem item = menu.findItem(R.id.post_list_action_enter_board);
    if (SMTHApplication.FROM_BOARD_BOARD.equals(mFrom)) {
      // from BoardTopicActivity
      item.setVisible(false);
    } else if (SMTHApplication.FROM_BOARD_HOT.equals(mFrom)) {
      // from HotTopicFragment
      item.setVisible(true);
    }
    return true;
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
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
    } else if (id == R.id.post_list_action_refresh) {
      reloadPostList();
    } else if (id == R.id.post_list_action_enter_board) {
      Board board = new Board();
      board.initAsBoard(mTopic.getBoardEngName(), mTopic.getBoardChsName(), "", "");
      Intent intent = new Intent(this, BoardTopicActivity.class);
      intent.putExtra(SMTHApplication.BOARD_OBJECT, (Parcelable) board);
      startActivity(intent);
    }
    return super.onOptionsItemSelected(item);
  }

  @Override public void onClick(View v) {
    // page navigation buttons
    switch (v.getId()) {
      case R.id.post_list_first_page:
        if (mCurrentPageNo == 1) {
          Toast.makeText(PostListActivity.this, "已在首页！", Toast.LENGTH_SHORT).show();
        } else {
          mCurrentPageNo = 1;
          reloadPostList();
        }
        break;
      case R.id.post_list_pre_page:
        if (mCurrentPageNo == 1) {
          Toast.makeText(PostListActivity.this, "已在首页！", Toast.LENGTH_SHORT).show();
        } else {
          mCurrentPageNo -= 1;
          reloadPostList();
        }
        break;
      case R.id.post_list_next_page:
        goToNextPage();
        break;
      case R.id.post_list_last_page:
        if (mCurrentPageNo == mTopic.getTotalPageNo()) {
          Toast.makeText(PostListActivity.this, "已在末页！", Toast.LENGTH_SHORT).show();
        } else {
          mCurrentPageNo = mTopic.getTotalPageNo();
          reloadPostList();
        }
        break;
      case R.id.post_list_go_page:
        int pageNo;
        try {
          pageNo = Integer.parseInt(mPageNo.getText().toString());
          if (mCurrentPageNo == pageNo) {
            Toast.makeText(PostListActivity.this, String.format("已在第%d页！", pageNo), Toast.LENGTH_SHORT).show();
          } else if (pageNo >= 1 && pageNo <= mTopic.getTotalPageNo()) {
            mCurrentPageNo = pageNo;
            // turn off keyboard
            mPageNo.clearFocus();
            InputMethodManager im = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            im.hideSoftInputFromWindow(mPageNo.getWindowToken(), 0);
            // jump now
            reloadPostList();
          } else {
            Toast.makeText(PostListActivity.this, "非法页码！", Toast.LENGTH_SHORT).show();
          }
        } catch (Exception e) {
          Toast.makeText(PostListActivity.this, "非法输入！", Toast.LENGTH_SHORT).show();
        }
        break;
      case R.id.post_list_action_top:
        mRecyclerView.scrollToPosition(0);
        break;
      case R.id.post_list_action_up:
        int prevPos = linearLayoutManager.findFirstVisibleItemPosition() - 1;
        if (prevPos >= 0) {
          mRecyclerView.smoothScrollToPosition(prevPos);
        }
        break;
      case R.id.post_list_action_down:
        int nextPos = linearLayoutManager.findLastVisibleItemPosition() + 1;
        if (nextPos < mRecyclerView.getAdapter().getItemCount()) {
          mRecyclerView.smoothScrollToPosition(nextPos);
        }
        break;
      case R.id.post_list_action_bottom:
        mRecyclerView.scrollToPosition(mRecyclerView.getAdapter().getItemCount() - 1);
        break;
    }
  }

  public void goToNextPage() {
    if (mCurrentPageNo == mTopic.getTotalPageNo()) {
      Toast.makeText(PostListActivity.this, "已在末页！", Toast.LENGTH_SHORT).show();
      clearLoadingHints();
    } else {
      mCurrentPageNo += 1;
      reloadPostList();
    }
  }

  @Override public boolean onKeyDown(int keyCode, KeyEvent event) {
    if (Settings.getInstance().isVolumeKeyScroll() && (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
      RecyclerViewUtil.ScrollRecyclerViewByKey(mRecyclerView, keyCode);
      return true;
    }
    return super.onKeyDown(keyCode, event);
  }

  // http://stackoverflow.com/questions/4500354/control-volume-keys
  @Override public boolean onKeyUp(int keyCode, KeyEvent event) {
    // disable the beep sound when volume up/down is pressed
    if (Settings.getInstance().isVolumeKeyScroll() && (keyCode == KeyEvent.KEYCODE_VOLUME_UP || keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
      return true;
    }
    return super.onKeyUp(keyCode, event);
  }

  @Override public boolean onItemLongClicked(final int position, View v) {
    if (position == RecyclerView.NO_POSITION || position >= PostListContent.POSTS.size()) return false;
//    Log.d(TAG, String.format("Post by %s is long clicked", PostListContent.POSTS.get(position).getAuthor()));
    List<PostActionAlertDialogItem> menuItemsArray = new ArrayList<PostActionAlertDialogItem>();
    menuItemsArray.add(new PostActionAlertDialogItem(getString(R.string.post_reply_post), R.drawable.ic_reply_black_48dp));  // 0
    menuItemsArray.add(new PostActionAlertDialogItem(getString(R.string.post_like_post), R.drawable.like_black)); // 1
    menuItemsArray.add(new PostActionAlertDialogItem(getString(R.string.post_reply_mail), R.drawable.ic_email_black_48dp)); // 2
    menuItemsArray.add(new PostActionAlertDialogItem(getString(R.string.post_query_author), R.drawable.ic_person_black_48dp)); // 3
    menuItemsArray.add(new PostActionAlertDialogItem(getString(R.string.post_filter_author), R.drawable.ic_find_in_page_black_48dp)); // 4
    menuItemsArray.add(new PostActionAlertDialogItem(getString(R.string.post_copy_content), R.drawable.ic_content_copy_black_48dp)); // 5
    menuItemsArray.add(new PostActionAlertDialogItem(getString(R.string.post_foward), R.drawable.ic_send_black_48dp)); // 6
    menuItemsArray.add(new PostActionAlertDialogItem(getString(R.string.post_view_in_browser), R.drawable.ic_open_in_browser_black_48dp)); // 7
    menuItemsArray.add(new PostActionAlertDialogItem(getString(R.string.post_share), R.drawable.ic_share_black_48dp)); // 8
    menuItemsArray.add(new PostActionAlertDialogItem(getString(R.string.post_delete_post), R.drawable.ic_delete_black_48dp));  // 9
    menuItemsArray.add(new PostActionAlertDialogItem(getString(R.string.post_edit_post), R.drawable.ic_edit_black_48dp)); // 10
    menuItemsArray.add(new PostActionAlertDialogItem(getString(R.string.post_convert_image), R.drawable.ic_photo_black_48dp)); // 11

    if(Settings.getInstance().isBoardMasterOnly()){
      menuItemsArray.add(new PostActionAlertDialogItem(getString(R.string.post_topic_delete), R.drawable.baseline_admin_panel_settings_black_48)); // 12
      menuItemsArray.add(new PostActionAlertDialogItem(getString(R.string.post_ban_id), R.drawable.baseline_admin_panel_settings_black_48));  // 13
      menuItemsArray.add(new PostActionAlertDialogItem(getString(R.string.post_mark_m), R.drawable.baseline_admin_panel_settings_black_48)); // 14
      menuItemsArray.add(new PostActionAlertDialogItem(getString(R.string.post_topic_readonly), R.drawable.baseline_admin_panel_settings_black_48)); // 15
    }

    PostActionAlertDialogItem[] menuItems = new PostActionAlertDialogItem[menuItemsArray.size()];
    menuItemsArray.toArray(menuItems);

    ListAdapter adapter = new ArrayAdapter<PostActionAlertDialogItem>(getApplicationContext(), R.layout.post_popup_menu_item, menuItems) {
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

    AlertDialog dialog = new AlertDialog.Builder(this).setTitle(getString(R.string.post_alert_title))
        .setAdapter(adapter, new DialogInterface.OnClickListener() {
          public void onClick(DialogInterface dialog, int which) {
            onPostPopupMenuItem(position, which);
          }
        })
        .create();
    dialog.setCanceledOnTouchOutside(true);
    dialog.setCancelable(true);

    dialog.show();
    return true;
  }

  private void onPostPopupMenuItem(int position, int which) {
    //        Log.d(TAG, String.format("MenuItem %d was clicked", which));
    if (position >= PostListContent.POSTS.size()) {
      Log.e(TAG, "onPostPopupMenuItem: " + "Invalid Post index" + position);
      return;
    }

    Post post = PostListContent.POSTS.get(position);
    if (which == 0) {
      // post_reply_post
      ComposePostContext postContext = new ComposePostContext();
      postContext.setBoardEngName(mTopic.getBoardEngName());
      postContext.setPostId(post.getPostID());
      postContext.setPostTitle(mTopic.getTitle());
      postContext.setPostAuthor(post.getRawAuthor());
      postContext.setPostContent(post.getRawContent());
      postContext.setComposingMode(ComposePostContext.MODE_REPLY_POST);

      Intent intent = new Intent(this, ComposePostActivity.class);
      intent.putExtra(SMTHApplication.COMPOSE_POST_CONTEXT, postContext);
      startActivityForResult(intent, ComposePostActivity.COMPOSE_ACTIVITY_REQUEST_CODE);
    } else if (which == 1) {
      // like
      // Toast.makeText(PostListActivity.this, "Like:TBD", Toast.LENGTH_SHORT).show();
      PopupLikeWindow popup = new PopupLikeWindow();
      popup.initPopupWindow(this);
      popup.showAtLocation(mRecyclerView, Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 100);
    } else if (which == 2) {
      // post_reply_mail
      // Toast.makeText(PostListActivity.this, "回复到作者信箱:TBD", Toast.LENGTH_SHORT).show();
      ComposePostContext postContext = new ComposePostContext();
      postContext.setBoardEngName(mTopic.getBoardEngName());
      postContext.setPostId(post.getPostID());
      postContext.setPostTitle(mTopic.getTitle());
      postContext.setPostAuthor(post.getRawAuthor());
      postContext.setPostContent(post.getRawContent());
      postContext.setComposingMode(ComposePostContext.MODE_REPLY_MAIL);

      Intent intent = new Intent(this, ComposePostActivity.class);
      intent.putExtra(SMTHApplication.COMPOSE_POST_CONTEXT, postContext);
      startActivity(intent);
    } else if (which == 3) {
      // post_query_author
      Intent intent = new Intent(this, QueryUserActivity.class);
      intent.putExtra(SMTHApplication.QUERY_USER_INFO, post.getRawAuthor());
      startActivity(intent);
    } else if (which == 4) {
      // read posts from current users only
      if (mFilterUser == null) {
        Toast.makeText(PostListActivity.this, "只看此ID! 再次选择将查看所有文章.", Toast.LENGTH_SHORT).show();
        mFilterUser = post.getRawAuthor();
      } else {
        Toast.makeText(PostListActivity.this, "查看所有文章!", Toast.LENGTH_SHORT).show();
        mFilterUser = null;
      }
      mCurrentPageNo = 1;
      reloadPostList();
    } else if (which == 5) {
      // copy post content
      // http://stackoverflow.com/questions/8056838/dealing-with-deprecated-android-text-clipboardmanager
      String content;
      if (post != null) {
        content = post.getRawContent();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
          final android.content.ClipboardManager clipboardManager =
              (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
          final android.content.ClipData clipData = android.content.ClipData.newPlainText("PostContent", content);
          clipboardManager.setPrimaryClip(clipData);
        } else {
          final android.text.ClipboardManager clipboardManager =
              (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
          clipboardManager.setText(content);
        }
        Toast.makeText(PostListActivity.this, "帖子内容已复制到剪贴板", Toast.LENGTH_SHORT).show();
      } else {
        Toast.makeText(PostListActivity.this, "复制失败！", Toast.LENGTH_SHORT).show();
      }
    } else if (which == 6) {
      // post_foward_self
      // Toast.makeText(PostListActivity.this, "转寄信箱:TBD", Toast.LENGTH_SHORT).show();
      PopupForwardWindow popup = new PopupForwardWindow();
      popup.initPopupWindow(this, post);
      popup.showAtLocation(mRecyclerView, Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 100);
    } else if (which == 7) {
      // open post in browser
      String url = String.format(SMTHHelper.SMTH_MOBILE_URL + "/article/%s/%s?p=%d", mTopic.getBoardEngName(), mTopic.getTopicID(), mCurrentPageNo);
      startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    } else if (which == 8) {
      // post_share
      // Toast.makeText(PostListActivity.this, "分享:TBD", Toast.LENGTH_SHORT).show();
      sharePost(post);
    } else if (which == 9) {
      // delete post
      deletePost(post);
    } else if (which == 10) {
      // edit post
      ComposePostContext postContext = new ComposePostContext();
      postContext.setBoardEngName(mTopic.getBoardEngName());
      postContext.setPostId(post.getPostID());
      postContext.setPostTitle(mTopic.getTitle());
      postContext.setPostAuthor(post.getRawAuthor());
      postContext.setPostContent(post.getRawContent());
      postContext.setComposingMode(ComposePostContext.MODE_EDIT_POST);

      Intent intent = new Intent(this, ComposePostActivity.class);
      intent.putExtra(SMTHApplication.COMPOSE_POST_CONTEXT, postContext);
      startActivity(intent);
    } else if (which == 11) {
      // generate screenshot of current post
      View v = mRecyclerView.getLayoutManager().findViewByPosition(position);

      // convert title + post to image
      captureView(mTitle, v, post.getPostID());
    } else if (which == 12) {
      deleteTopic(post);
    } else if (which == 13) {
      PopupBanWindow popup = new PopupBanWindow();
      popup.initPopupWindow(this, post);
      popup.showAtLocation(mRecyclerView, Gravity.CENTER_HORIZONTAL | Gravity.CENTER_VERTICAL, 0, 100);
    } else if (which == 14) {
      markPost(post);
    } else if (which == 15) {
      readonlyTopic(post);
    }
  }

  void captureView(View v1, View v2, String postID) {
    capView1 = v1;
    capView2 = v2;
    capPostID = postID;
    captureViewInternal();
  }

  @AfterPermissionGranted(RC_READ_WRITE_STORAGE)
  void captureViewInternal(){
    String[] perms = {Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
    if (EasyPermissions.hasPermissions(this, perms)) {
      // Already have permission, do the thing
      //Create a Bitmap with the same dimensions
      Bitmap image = Bitmap.createBitmap(capView1.getWidth(), capView1.getHeight() + capView2.getHeight(), Bitmap.Config.RGB_565);
      //Draw the view inside the Bitmap
      Canvas canvas = new Canvas(image);

      if(Settings.getInstance().isNightMode()) {
        canvas.drawColor(Color.BLACK);
      } else {
        canvas.drawColor(Color.WHITE);
      }
      capView1.draw(canvas);
      canvas.translate(0, capView1.getHeight());
      capView2.draw(canvas);
      canvas.save();

      // save image to sdcard
      try {
        if (TextUtils.equals(Environment.getExternalStorageState(), Environment.MEDIA_MOUNTED)) {
          String path = Environment.getExternalStorageDirectory().getPath() + "/zSMTH/";
          File dir = new File(path);
          if (!dir.exists()) {
            dir.mkdirs();
          }

          String IMAGE_FILE_PREFIX = "post-";
          String IMAGE_FILE_SUFFIX = ".jpg";
          File outFile = new File(dir, IMAGE_FILE_PREFIX + capPostID + IMAGE_FILE_SUFFIX);
          FileOutputStream out = new FileOutputStream(outFile);

          image.compress(Bitmap.CompressFormat.JPEG, 90, out); //Output
          Toast.makeText(PostListActivity.this, "截图已存为: /zSMTH/" + outFile.getName(), Toast.LENGTH_SHORT).show();

          // make sure the new file can be recognized soon
          sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(outFile)));
        }
      } catch (Exception e) {
        Log.e(TAG, "saveImageToFile: " + Log.getStackTraceString(e));
        Toast.makeText(PostListActivity.this, "保存截图失败:\n请授予应用存储权限！\n" + e.toString(), Toast.LENGTH_LONG).show();
      }
    } else {
      // Do not have permissions, request them now
      EasyPermissions.requestPermissions(this, getString(R.string.read_write_storage_rationale),
              RC_READ_WRITE_STORAGE, perms);
    }
  }

  public void deletePost(Post post) {
    SMTHHelper helper = SMTHHelper.getInstance();

    helper.wService.deletePost(mTopic.getBoardEngName(), post.getPostID()).map(new Function<ResponseBody, String>() {
      @Override public String apply(@NonNull ResponseBody responseBody) throws Exception {
        try {
          String response = SMTHHelper.DecodeResponseFromWWW(responseBody.bytes());
          return SMTHHelper.parseDeleteResponse(response);
        } catch (Exception e) {
          Log.e(TAG, "call: " + Log.getStackTraceString(e));
        }
        return null;
      }
    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
      @Override public void onSubscribe(@NonNull Disposable disposable) {

      }

      @Override public void onNext(@NonNull String s) {
          Toast.makeText(PostListActivity.this, s, Toast.LENGTH_LONG).show();
      }

      @Override public void onError(@NonNull Throwable e) {
        Toast.makeText(PostListActivity.this, "删除帖子失败！\n" + e.toString(), Toast.LENGTH_LONG).show();

      }

      @Override public void onComplete() {

      }
    });
  }

  public void deleteTopic(Post post) {
    SMTHHelper helper = SMTHHelper.getInstance();

    helper.wService.deleteTopic(mTopic.getBoardEngName(), post.getPostID(), mTopic.getTopicID(), "d")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<AjaxResponse>() {
      @Override public void onSubscribe(@NonNull Disposable disposable) {

      }

      @Override public void onNext(@NonNull AjaxResponse ajaxResponse) {
        if (ajaxResponse.getAjax_st() == AjaxResponse.AJAX_RESULT_OK) {
          Toast.makeText(PostListActivity.this, ajaxResponse.getAjax_msg(), Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(PostListActivity.this, ajaxResponse.toString(), Toast.LENGTH_LONG).show();
        }
      }

      @Override public void onError(@NonNull Throwable e) {
        Toast.makeText(PostListActivity.this, "删除主题失败！\n" + e.toString(), Toast.LENGTH_LONG).show();

      }

      @Override public void onComplete() {

      }
    });
  }

  public void OnBanIDAction(Post post, String banReason, Integer day) {
    SMTHHelper helper = SMTHHelper.getInstance();

    helper.wService.banID(mTopic.getBoardEngName(), post.getPostID(), banReason, day)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<AjaxResponse>() {
      @Override public void onSubscribe(@NonNull Disposable disposable) {

      }

      @Override public void onNext(@NonNull AjaxResponse ajaxResponse) {
        if (ajaxResponse.getAjax_st() == AjaxResponse.AJAX_RESULT_OK) {
          Toast.makeText(PostListActivity.this, ajaxResponse.getAjax_msg(), Toast.LENGTH_SHORT).show();
        } else {
          Toast.makeText(PostListActivity.this, ajaxResponse.toString(), Toast.LENGTH_LONG).show();
        }
      }

      @Override public void onError(@NonNull Throwable e) {
        Toast.makeText(PostListActivity.this, "封禁ID失败！\n" + e.toString(), Toast.LENGTH_LONG).show();

      }

      @Override public void onComplete() {

      }
    });
  }

  public void sharePost(Post post) {
    OnekeyShare oks = new OnekeyShare();
    //关闭sso授权
    oks.disableSSOWhenAuthorize();

    // prepare information from the post
    String title = String.format("[%s] %s @ 水木社区", mTopic.getBoardChsName(), mTopic.getTitle());
    String postURL =
        String.format(SMTHHelper.SMTH_MOBILE_URL + "/article/%s/%s?p=%d", mTopic.getBoardEngName(), mTopic.getTopicID(), mCurrentPageNo);
    String content = String.format("[%s]在大作中写到: %s", post.getAuthor(), post.getRawContent());
    // the max length of webo is 140
    if (content.length() > 110) {
      content = content.substring(0, 110);
    }
    content += String.format("...\nLink:%s", postURL);

    // default: use zSMTH logo
    String imageURL = "http://zsmth.zfdang.com/zsmth.png";
    List<Attachment> attaches = post.getAttachFiles();
    if (attaches != null && attaches.size() > 0) {
      // use the first attached image
      imageURL = attaches.get(0).getResizedImageSource();
    }

    // more information about OnekeyShare
    // http://wiki.mob.com/docs/sharesdk/android/cn/sharesdk/onekeyshare/OnekeyShare.html

    // title标题，印象笔记、邮箱、信息、微信、人人网、QQ和QQ空间使用
    oks.setTitle(title);

    // titleUrl是标题的网络链接，仅在Linked-in,QQ和QQ空间使用
    // oks.setTitleUrl("http://sharesdk.cn");

    // text是分享文本，所有平台都需要这个字段
    oks.setText(content);

    // 分享网络图片，新浪微博分享网络图片需要通过审核后申请高级写入接口，否则请注释掉测试新浪微博
    // imageUrl是图片的网络路径，新浪微博、人人网、QQ空间和Linked-In支持此字段
    oks.setImageUrl(imageURL);

    // imagePath是图片的本地路径，Linked-In以外的平台都支持此参数
    //oks.setImagePath("/sdcard/test.jpg");//确保SDcard下面存在此张图片

    // url仅在微信（包括好友和朋友圈）中使用
    oks.setUrl(postURL);

    // comment是我对这条分享的评论，仅在人人网和QQ空间使用
    //        oks.setComment("我是测试评论文本");
    // site是分享此内容的网站名称，仅在QQ空间使用
    //        oks.setSite("ShareSDK");
    // siteUrl是分享此内容的网站地址，仅在QQ空间使用
    //        oks.setSiteUrl("http://sharesdk.cn");

    // set callback functions
    oks.setCallback(new PlatformActionListener() {
      @Override public void onComplete(Platform platform, int i, HashMap<String, Object> hashMap) {
        Toast.makeText(PostListActivity.this, "分享成功!", Toast.LENGTH_SHORT).show();
      }

      @Override public void onError(Platform platform, int i, Throwable throwable) {
        Toast.makeText(PostListActivity.this, "分享失败:\n" + throwable.toString(), Toast.LENGTH_LONG).show();
      }

      @Override public void onCancel(Platform platform, int i) {
      }
    });

    // 启动分享GUI
    oks.show(this);
  }

  @Override public boolean onTouch(View v, MotionEvent event) {
    mGestureDetector.onTouchEvent(event);
    return false;
  }

  @Override public void OnLikeAction(String score, String msg) {
    //        Log.d(TAG, "OnLikeAction: " + score + msg);

    SMTHHelper helper = SMTHHelper.getInstance();
    helper.wService.addLike(mTopic.getBoardEngName(), mTopic.getTopicID(), score, msg, "")
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<AjaxResponse>() {
          @Override public void onSubscribe(@NonNull Disposable disposable) {

          }

          @Override public void onNext(@NonNull AjaxResponse ajaxResponse) {
            // Log.d(TAG, "onNext: " + ajaxResponse.toString());
            if (ajaxResponse.getAjax_st() == AjaxResponse.AJAX_RESULT_OK) {
              Toast.makeText(PostListActivity.this, ajaxResponse.getAjax_msg(), Toast.LENGTH_SHORT).show();
              reloadPostList();
            } else {
              Toast.makeText(PostListActivity.this, ajaxResponse.toString(), Toast.LENGTH_LONG).show();
            }
          }

          @Override public void onError(@NonNull Throwable e) {
            Toast.makeText(PostListActivity.this, "增加Like失败!\n" + e.toString(), Toast.LENGTH_LONG).show();
          }

          @Override public void onComplete() {

          }
        });
  }

  @Override public void OnForwardAction(Post post, String target, boolean threads, boolean noref, boolean noatt) {
    //        Log.d(TAG, "OnForwardAction: ");

    String strThreads = null;
    if (threads) strThreads = "on";
    String strNoref = null;
    if (noref) strNoref = "on";
    String strNoatt = null;
    if (noatt) strNoatt = "on";
    String strNoansi = null;
    if (target != null && target.contains("@")) strNoansi = "on";

    SMTHHelper helper = SMTHHelper.getInstance();
    helper.wService.forwardPost(mTopic.getBoardEngName(), post.getPostID(), target, strThreads, strNoref, strNoatt, strNoansi)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Observer<AjaxResponse>() {
          @Override public void onSubscribe(@NonNull Disposable disposable) {

          }

          @Override public void onNext(@NonNull AjaxResponse ajaxResponse) {
            // Log.d(TAG, "onNext: " + ajaxResponse.toString());
            if (ajaxResponse.getAjax_st() == AjaxResponse.AJAX_RESULT_OK) {
              Toast.makeText(PostListActivity.this, ajaxResponse.getAjax_msg(), Toast.LENGTH_SHORT).show();
            } else {
              Toast.makeText(PostListActivity.this, ajaxResponse.toString(), Toast.LENGTH_LONG).show();
            }
          }

          @Override public void onError(@NonNull Throwable e) {
            Toast.makeText(PostListActivity.this, "转寄失败！\n" + e.toString(), Toast.LENGTH_LONG).show();
          }

          @Override public void onComplete() {

          }
        });
  }

  @Override public void OnRePostAction(Post post, String target, String outgo) {
    SMTHHelper helper = SMTHHelper.getInstance();
    helper.wService.repostPost(mTopic.getBoardEngName(), post.getPostID(), target, outgo).map(new Function<ResponseBody, String>() {
      @Override public String apply(@NonNull ResponseBody responseBody) throws Exception {
        try {
          String response = SMTHHelper.DecodeResponseFromWWW(responseBody.bytes());
          return SMTHHelper.parseRepostResponse(response);
        } catch (Exception e) {
          Log.e(TAG, "call: " + Log.getStackTraceString(e));
        }
        return null;
      }
    }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<String>() {
      @Override public void onSubscribe(@NonNull Disposable disposable) {

      }

      @Override public void onNext(@NonNull String s) {
        Toast.makeText(SMTHApplication.getAppContext(), s, Toast.LENGTH_SHORT).show();
      }

      @Override public void onError(@NonNull Throwable e) {
        Toast.makeText(SMTHApplication.getAppContext(), e.toString(), Toast.LENGTH_LONG).show();
      }

      @Override public void onComplete() {

      }
    });
  }

  public void markPost(Post post) {
    SMTHHelper helper = SMTHHelper.getInstance();

    helper.wService.markPost(mTopic.getBoardEngName(), post.getPostID(), mTopic.getTopicID(), "m")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<AjaxResponse>() {
              @Override public void onSubscribe(@NonNull Disposable disposable) {

              }

              @Override public void onNext(@NonNull AjaxResponse ajaxResponse) {
                if (ajaxResponse.getAjax_st() == AjaxResponse.AJAX_RESULT_OK) {
                  Toast.makeText(PostListActivity.this, ajaxResponse.getAjax_msg(), Toast.LENGTH_SHORT).show();
                } else {
                  Toast.makeText(PostListActivity.this, ajaxResponse.toString(), Toast.LENGTH_LONG).show();
                }
              }

              @Override public void onError(@NonNull Throwable e) {
                Toast.makeText(PostListActivity.this, "设置/取消m标记！\n" + e.toString(), Toast.LENGTH_LONG).show();

              }

              @Override public void onComplete() {

              }
            });
  }

  public void readonlyTopic(Post post) {
    SMTHHelper helper = SMTHHelper.getInstance();

    helper.wService.readonlyTopic(mTopic.getBoardEngName(), post.getPostID(), mTopic.getTopicID(), ";")
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(new Observer<AjaxResponse>() {
              @Override public void onSubscribe(@NonNull Disposable disposable) {

              }

              @Override public void onNext(@NonNull AjaxResponse ajaxResponse) {
                if (ajaxResponse.getAjax_st() == AjaxResponse.AJAX_RESULT_OK) {
                  Toast.makeText(PostListActivity.this, ajaxResponse.getAjax_msg(), Toast.LENGTH_SHORT).show();
                } else {
                  Toast.makeText(PostListActivity.this, ajaxResponse.toString(), Toast.LENGTH_LONG).show();
                }
              }

              @Override public void onError(@NonNull Throwable e) {
                Toast.makeText(PostListActivity.this, "设置同主题不可回复！\n" + e.toString(), Toast.LENGTH_LONG).show();

              }

              @Override public void onComplete() {

              }
            });
  }
}
