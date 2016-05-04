package com.zfdang.zsmth_android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.util.Linkify;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jude.swipbackhelper.SwipeBackHelper;
import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.fresco.WrapContentDraweeView;
import com.zfdang.zsmth_android.models.Attachment;
import com.zfdang.zsmth_android.models.ComposePostContext;
import com.zfdang.zsmth_android.models.ContentSegment;
import com.zfdang.zsmth_android.models.Mail;
import com.zfdang.zsmth_android.models.Post;
import com.zfdang.zsmth_android.newsmth.AjaxResponse;
import com.zfdang.zsmth_android.newsmth.SMTHHelper;
import com.zfdang.zsmth_android.view.LinkTextView;

import java.util.ArrayList;
import java.util.List;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class MailContentActivity extends AppCompatActivity {

    private static final String TAG = "MailContent";
    private Mail mMail;
    private Post mPost;

    public TextView mPostAuthor;
    public TextView mPostIndex;
    public TextView mPostPublishDate;
    private LinearLayout mViewGroup;
    public LinkTextView mPostContent;

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

        setContentView(R.layout.activity_mail_content);

        // init post widget
        mPostAuthor = (TextView) findViewById(R.id.post_author);
        mPostIndex = (TextView) findViewById(R.id.post_index);
        mPostIndex.setVisibility(View.GONE);
        mPostPublishDate = (TextView) findViewById(R.id.post_publish_date);
        mViewGroup = (LinearLayout) findViewById(R.id.post_content_holder);
        mPostContent = (LinkTextView) findViewById(R.id.post_content);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        // load mMail content
        Bundle bundle = getIntent().getExtras();
        mMail = (Mail) bundle.getParcelable(SMTHApplication.MAIL_OBJECT);
        loadMailContent();
    }

    public void loadMailContent() {
        SMTHHelper helper = SMTHHelper.getInstance();
        helper.wService.getMailContent(mMail.url)
                .map(new Func1<AjaxResponse, Post>() {
                    @Override
                    public Post call(AjaxResponse ajaxResponse) {
                        return SMTHHelper.ParseMailContentFromWWW(ajaxResponse.getContent());
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Post>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.e(TAG, "onError: " + Log.getStackTraceString(e) );
                        mPostContent.setText(Log.getStackTraceString(e));
                    }

                    @Override
                    public void onNext(Post post) {
                        mPost = post;

                        // copy some attr from mail to post
                        mPost.setAuthor(mMail.author);
                        mPost.setTitle(mMail.title);
                        mPost.setPostID(mMail.getMailIDFromURL());

                        mPostAuthor.setText(mPost.getRawAuthor());
                        mPostPublishDate.setText(mPost.getFormatedDate());
                        inflateContentViewGroup(mViewGroup, mPostContent, mPost);
                    }
                });

    }

    //    copied from PostRecyclerViewAdapter.inflateContentViewGroup, almost the same code
    public void inflateContentViewGroup(ViewGroup viewGroup, TextView contentView, final Post post) {
        // remove all child view in viewgroup
        viewGroup.removeAllViews();

        List<ContentSegment> contents = post.getContentSegments();
        if(contents == null) return;

        // the simple case, without any attachment
        if(contents.size() == 1) {
            viewGroup.addView(contentView);
            contentView.setText(contents.get(0).getSpanned());
            Linkify.addLinks(contentView, Linkify.WEB_URLS);

            return;
        }

        // there are multiple segments, add the first contentView first
        // contentView is always available, we don't have to inflate it again
        viewGroup.addView(contentView);
        contentView.setText(contents.get(0).getSpanned());

        final LayoutInflater inflater = (LayoutInflater) SMTHApplication.getAppContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        for(int i = 1; i < contents.size(); i++) {
            ContentSegment content = contents.get(i);

            if(content.getType() == ContentSegment.SEGMENT_IMAGE) {
                // Log.d("CreateView", "Image: " + content.getUrl());

                // Add the text layout to the parent layout
                WrapContentDraweeView image = (WrapContentDraweeView) inflater.inflate(R.layout.post_item_imageview, viewGroup, false);
                image.setImageFromStringURL(content.getUrl());


                // set onclicklistener
                image.setTag(R.id.image_tag, content.getImgIndex());
                image.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        int position = (int) v.getTag(R.id.image_tag);

                        Intent intent = new Intent(MailContentActivity.this, FSImageViewerActivity.class);

                        ArrayList<String> urls = new ArrayList<>();
                        List<Attachment> attaches = post.getAttachFiles();
                        for (Attachment attach: attaches) {
                            urls.add(attach.getImageSrc());
                        }

                        intent.putStringArrayListExtra(SMTHApplication.ATTACHMENT_URLS, urls);
                        intent.putExtra(SMTHApplication.ATTACHMENT_CURRENT_POS, position);
                        startActivity(intent);
                    }
                });

                // Add the text view to the parent layout
                viewGroup.addView(image);
            } else if (content.getType() == ContentSegment.SEGMENT_TEXT) {
                // Log.d("CreateView", "Text: " + content.getSpanned().toString());

                // Add the text layout to the parent layout
                LinkTextView tv = (LinkTextView) inflater.inflate(R.layout.post_item_content, viewGroup, false);
                tv.setText(content.getSpanned());
                Linkify.addLinks(tv, Linkify.WEB_URLS);

                // Add the text view to the parent layout
                viewGroup.addView(tv);
            }
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            onBackPressed();
            return true;
        } else if (id == R.id.mail_content_reply) {
            ComposePostContext postContext = new ComposePostContext();
            postContext.setPostid(mPost.getPostID());
            postContext.setPostTitle(mPost.getTitle());
            postContext.setPostAuthor(mPost.getRawAuthor());
            postContext.setPostContent(mPost.getRawContent());
            postContext.setThroughMail(true);

            Intent intent = new Intent(this, ComposePostActivity.class);
            intent.putExtra(SMTHApplication.COMPOSE_POST_CONTEXT, postContext);
            startActivity(intent);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.mail_content_menu, menu);
        return true;
    }

}
