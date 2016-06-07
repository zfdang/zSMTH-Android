package com.zfdang.zsmth_android;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.zfdang.SMTHApplication;
import com.zfdang.multiple_images_selector.ImagesSelectorActivity;
import com.zfdang.multiple_images_selector.SelectorSettings;
import com.zfdang.zsmth_android.helpers.KeyboardLess;
import com.zfdang.zsmth_android.helpers.StringUtils;
import com.zfdang.zsmth_android.models.ComposePostContext;
import com.zfdang.zsmth_android.newsmth.AjaxResponse;
import com.zfdang.zsmth_android.newsmth.SMTHHelper;

import java.util.ArrayList;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class ComposePostActivity extends SMTHBaseActivity {

    private static final int REQUEST_CODE = 653;
    private static final String TAG = "ComposePostActivity";
    private final String UPLOAD_TEMPLATE = "  [upload=%d][/upload]  ";

    private Button mButton;
    private LinearLayout mUserRow;
    private EditText mUserID;
    private EditText mTitle;
    private LinearLayout mAttachRow;
    private EditText mAttachments;
    private EditText mContent;
    private ArrayList<String> mPhotos;
    private TextView mContentCount;

    private ComposePostContext mPostContext;

    // used to show progress while publishing
    private static int  totalSteps = 1;
    private static int currentStep = 1;

    private int postPublishResult = 0;
    private String postPublishMessage = null;
    private AjaxResponse lastResponse = null;


    public void startImageSelector() {
        // start multiple photos selector
        Intent intent = new Intent(ComposePostActivity.this, ImagesSelectorActivity.class);
        // max number of images to be selected
        intent.putExtra(SelectorSettings.SELECTOR_MAX_IMAGE_NUMBER, 5);
        // min size of image which will be shown; to filter tiny images (mainly icons)
        intent.putExtra(SelectorSettings.SELECTOR_MIN_IMAGE_SIZE, 100000);
        // show camera or not
        intent.putExtra(SelectorSettings.SELECTOR_SHOW_CAMERA, false);
        // pass current selected images as the initial value
        intent.putStringArrayListExtra(SelectorSettings.SELECTOR_INITIAL_SELECTED_LIST, mPhotos);
        // start the selector
        startActivityForResult(intent, REQUEST_CODE);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE){
            if (resultCode == RESULT_OK && data != null) {
                mPhotos = data.getStringArrayListExtra(SelectorSettings.SELECTOR_RESULTS);
                mAttachments.setText(String.format("共有%d个附件", mPhotos.size()));

                String attachments = "";
                for(int i = 0; i < mPhotos.size(); i ++) {
                    attachments += String.format(UPLOAD_TEMPLATE, i+1);
                }

                mContent.setText(attachments + mContent.getText().toString());
            }
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_compose_post);


        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);;

        mPhotos = new ArrayList<>();

        mUserRow = (LinearLayout) findViewById(R.id.compose_post_userid_row);
        mUserID = (EditText) findViewById(R.id.compose_post_userid);
        mTitle = (EditText) findViewById(R.id.compose_post_title);
        mAttachRow = (LinearLayout) findViewById(R.id.compose_post_attach_row);
        mAttachments = (EditText) findViewById(R.id.compose_post_attach);
        mContent = (EditText) findViewById(R.id.compose_post_content);
        mContentCount = (TextView) findViewById(R.id.compose_post_content_label);

        mButton = (Button) findViewById(R.id.compose_post_attach_button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startImageSelector();
            }
        });

        mContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) { }

            @Override
            public void afterTextChanged(Editable s) {
                mContentCount.setText(String.format("文章字数:%d", s.length()));
            }
        });

        // init controls from Intent
        initFromIntent();

        // open keypads
        KeyboardLess.$show(this, mContent);
    }

    public void initFromIntent() {
        // get ComposePostContext from caller
        Intent intent = getIntent();
        mPostContext = intent.getParcelableExtra(SMTHApplication.COMPOSE_POST_CONTEXT);
        assert mPostContext != null;
//        Log.d(TAG, "initFromIntent: " + mPostContext.toString());

        // there are totally 5 different cases: ( 3 & 4 can be handled in the same way)
        // 1. new post:   invalid post && !isThroughMail
        // 2. reply post: valid post && !isThroughMail
        // 3. reply post through mail: valid post && isThroughMail
        // 4. reply mail: valid post && isThroughMail
        // 5. write new mail: invalid post && isThroughMail

        // set title, hide userRow / attachRow if necessary
        if(mPostContext.getComposingMode() == ComposePostContext.MODE_NEW_POST) {
            mUserRow.setVisibility(View.GONE);
            setTitle(String.format("发表文章@%s", mPostContext.getBoardEngName()));
        } else if(mPostContext.getComposingMode() == ComposePostContext.MODE_REPLY_POST) {
            mUserRow.setVisibility(View.GONE);
            setTitle(String.format("回复文章@%s", mPostContext.getBoardEngName()));
        } else if(mPostContext.getComposingMode() == ComposePostContext.MODE_EDIT_POST) {
            mUserRow.setVisibility(View.GONE);
            mAttachRow.setVisibility(View.GONE);
            setTitle(String.format("修改文章@%s", mPostContext.getBoardEngName()));
        } else if(mPostContext.getComposingMode() == ComposePostContext.MODE_NEW_MAIL ){
            mAttachRow.setVisibility(View.GONE);
            setTitle("写新信件");
        } else if(mPostContext.getComposingMode() == ComposePostContext.MODE_NEW_MAIL_TO_USER ){
            mAttachRow.setVisibility(View.GONE);
            setTitle("写新信件");
            mUserID.setText(mPostContext.getPostAuthor());
            mUserID.setEnabled(false);
        } else if(mPostContext.getComposingMode() == ComposePostContext.MODE_REPLY_MAIL){
            mAttachRow.setVisibility(View.GONE);
            setTitle("回复信件");
            mUserID.setText(mPostContext.getPostAuthor());
            mUserID.setEnabled(false);
        }

        // set post title & content
        if(mPostContext.isValidPost()) {
            // have valid post information
            if(mPostContext.getComposingMode() == ComposePostContext.MODE_EDIT_POST) {
                mTitle.setText(mPostContext.getPostTitle());
                mContent.setText(mPostContext.getPostContent());
            } else {
                String title = mPostContext.getPostTitle();
                if(title != null && !title.startsWith("Re:")) {
                    title = String.format("Re: %s", title);
                }
                mTitle.setText(title);

                String[] lines = mPostContext.getPostContent().split("\n");
                StringBuilder wordList = new StringBuilder();
                wordList.append("\n\n");
                wordList.append(String.format("【 在 %s 的大作中提到: 】", mPostContext.getPostAuthor())).append("\n");
                for(int i = 0; i < lines.length && i < 5; i++) {
                    if(lines[i].startsWith("--")){
                        // this might be the start of signature, ignore following lines in quote
                        break;
                    }
                    wordList.append(String.format(": %s", lines[i])).append("\n");
                }
                mContent.setText(new String(wordList));
            }

            // focus content, and move cursor to the beginning
            mContent.requestFocus();
            mContent.setSelection(0);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.compose_post_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public void onBackPressed() {
        onBackAction();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int code = item.getItemId();
        if(code == android.R.id.home) {
            onBackAction();
        } else if (code == R.id.compose_post_publish) {
            publishPost();
        }
        return super.onOptionsItemSelected(item);
    }

    public class BytesContainer {
        public String filename;
        public byte[] bytes;

        public BytesContainer(String filename, byte[] bytes) {
            this.bytes = bytes;
            this.filename = filename;
        }
    }

    public void publishPost() {

        postPublishResult = AjaxResponse.AJAX_RESULT_OK;
        postPublishMessage = "";
        lastResponse = null;

        final String progressHint = "发表文章中(%d/%d)...";
        ComposePostActivity.totalSteps = 1;
        ComposePostActivity.currentStep = 1;
        if(mPhotos != null){
            ComposePostActivity.totalSteps += mPhotos.size();
        }

        showProgress(String.format(progressHint, ComposePostActivity.currentStep, ComposePostActivity.totalSteps));

        final SMTHHelper helper = SMTHHelper.getInstance();

        // update attachments
        Observable<AjaxResponse> resp1 = Observable.from(mPhotos)
                .map(new Func1<String, BytesContainer>() {
                    @Override
                    public BytesContainer call(String filename) {
                        // TODO: to support uploading GIF file
                        byte[] bytes = SMTHHelper.getBitmapBytesWithResize(filename);
                        return new BytesContainer(filename, bytes);
                    }
                })
                .map(new Func1<BytesContainer, AjaxResponse>() {
                    @Override
                    public AjaxResponse call(BytesContainer container) {
                        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), container.bytes);
                        List<AjaxResponse> resps = helper.wService.uploadAttachment(mPostContext.getBoardEngName(), StringUtils.getLastStringSegment(container.filename), requestBody)
                                .toList().toBlocking().single();
                        if (resps != null && resps.size() == 1) {
                            return resps.get(0);
                        } else {
                            Log.d(TAG, "call: " + "failed to upload attachment " + container.filename);
                            return null;
                        }
                    }
                });

        // publish post
        String postContent = mContent.getText().toString() + "\n" + String.format("#发自zSMTH@%s", Settings.getInstance().getSignature());
        Observable<AjaxResponse> resp2 = null;
        if (mPostContext.getComposingMode() == ComposePostContext.MODE_NEW_MAIL
                || mPostContext.getComposingMode() == ComposePostContext.MODE_NEW_MAIL_TO_USER
                || mPostContext.getComposingMode() == ComposePostContext.MODE_REPLY_MAIL) {
            String userid = mUserID.getText().toString().trim();
            resp2 = SMTHHelper.sendMail(userid, mTitle.getText().toString(), postContent);
        } else if (mPostContext.getComposingMode() == ComposePostContext.MODE_NEW_POST
                || mPostContext.getComposingMode() == ComposePostContext.MODE_REPLY_POST) {
            resp2 = SMTHHelper.publishPost(mPostContext.getBoardEngName(), mTitle.getText().toString(), postContent, "0", mPostContext.getPostId());
        } else if (mPostContext.getComposingMode() == ComposePostContext.MODE_EDIT_POST) {
            postContent = mContent.getText().toString() + "\n" + String.format("#修改自zSMTH@%s", Settings.getInstance().getSignature());
            resp2 = SMTHHelper.editPost(mPostContext.getBoardEngName(), mPostContext.getPostId(), mTitle.getText().toString(), postContent);
        }

        // process all these tasks one by one
        Observable.concat(resp1, resp2)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<AjaxResponse>() {
                    @Override
                    public void onCompleted() {
                        dismissProgress();

                        String message = null;
                        if(postPublishResult != AjaxResponse.AJAX_RESULT_OK) {
                            message = "操作失败! \n错误信息:\n" + postPublishMessage;
                            Toast.makeText(ComposePostActivity.this, message, Toast.LENGTH_LONG).show();
                        } else {
                            if(lastResponse != null) {
                                // if we have valid last response, use the message.
                                message = lastResponse.getAjax_msg();
                            } else {
                                // otherwise, compose the message by ourself
                                message = "成功!";
                            }
                            Toast.makeText(SMTHApplication.getAppContext(), message, Toast.LENGTH_LONG).show();

                            KeyboardLess.$hide(ComposePostActivity.this, mContent);

                            if(message != null && !message.contains("本文可能含有不当内容")) {
                                ComposePostActivity.this.finish();
                            }
                        }
                    }

                    @Override
                    public void onError(Throwable e) {
                        dismissProgress();
                        Toast.makeText(SMTHApplication.getAppContext(), "发生错误:\n" + e.toString(), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNext(AjaxResponse ajaxResponse) {
                        Log.d(TAG, "onNext: " + ajaxResponse.toString());
                        if(ajaxResponse.getAjax_st() != AjaxResponse.AJAX_RESULT_OK) {
                            postPublishResult = AjaxResponse.AJAX_RESULT_FAILED;
                            postPublishMessage += ajaxResponse.getAjax_msg() + "\n";
                        }
                        lastResponse = ajaxResponse;
                        ComposePostActivity.currentStep ++;
                        showProgress(String.format(progressHint, ComposePostActivity.currentStep, ComposePostActivity.totalSteps));
                    }
                });

    }

    public void onBackAction() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ComposePostActivity.this);
        alertDialogBuilder.setTitle("退出确认")
                .setMessage("结束编辑，或者停留在当前界面继续编辑？")
                .setCancelable(false)
                .setPositiveButton("结束编辑", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close current activity
                        KeyboardLess.$hide(ComposePostActivity.this, mContent);
                        ComposePostActivity.this.finish();
                    }
                })
                .setNegativeButton("继续编辑", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close the dialog box and do nothing
                        dialog.cancel();
                    }
                }).create().show();
    }

}
