package com.zfdang.zsmth_android;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.zfdang.SMTHApplication;
import com.zfdang.multiple_images_selector.ImagesSelectorActivity;
import com.zfdang.multiple_images_selector.SelectorSettings;
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

public class ComposePostActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 653;
    private static final String TAG = "ComposePostActivity";
    private final String UPLOAD_TEMPLATE = "    [upload=%d][/upload]\n";

    private ProgressDialog pdialog = null;

    private Button mButton;
    private EditText mTitle;
    private EditText mAttachments;
    private EditText mContent;
    private ArrayList<String> mPhotos;
    private TextView mContentCount;

    private ComposePostContext mPostContent;

    // used to show progress while publishing
    private static int  totalSteps = 1;
    private static int currentStep = 1;

    private int postPublishResult = 0;
    private String postPUblishMessage = null;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE){
            if (resultCode == RESULT_OK && data != null) {
                mPhotos = data.getStringArrayListExtra(SelectorSettings.SELECTOR_RESULTS);
                mAttachments.setText(String.format("共有%d个附件", mPhotos.size()));
//                for (String filename: mPhotos) {
//                    Log.d(TAG, "onActivityResult: " + filename);
//                }

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

        mTitle = (EditText) findViewById(R.id.compose_post_title);
        mAttachments = (EditText) findViewById(R.id.compose_post_attach);
        mContent = (EditText) findViewById(R.id.compose_post_content);
        mContentCount = (TextView) findViewById(R.id.compose_post_content_label);

        mButton = (Button) findViewById(R.id.compose_post_attach_button);
        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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
        });

        mContent.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                mContentCount.setText(String.format("文章字数:%d", s.length()));
            }
        });

        // init widgets from Internt
        initFromIntent();
    }

    public void initFromIntent() {
        // get ComposePostContext from caller
        Intent intent = getIntent();
        mPostContent = intent.getParcelableExtra(SMTHApplication.COMPOSE_POST_CONTEXT);
        assert mPostContent != null;

//        Log.d(TAG, "initFromIntent: " + mPostContent.toString());

        if(mPostContent.getPostid() != null && mPostContent.getPostid().length() > 0) {
            setTitle(String.format("回复文章@%s", mPostContent.getBoardEngName()));
            mTitle.setText(String.format("Re: %s", mPostContent.getPostTitle()));

            String[] lines = mPostContent.getPostContent().split("\n");
            StringBuilder wordList = new StringBuilder();
            wordList.append("\n\n");
            wordList.append(String.format("【 在 %s 的大作中提到: 】", mPostContent.getPostAuthor())).append("\n");
            for(int i = 0; i < lines.length && i < 5; i++) {
                if(lines[i].startsWith("--")){
                    // this might be the start of signature, ignore following lines in quote
                    break;
                }
                wordList.append(String.format(": %s", lines[i])).append("\n");
            }
            mContent.setText(new String(wordList));

            // focus content, and move cursor to the beginning
            mContent.requestFocus();
            mContent.setSelection(0);
        } else {
            mPostContent.setPostid("0");
            setTitle(String.format("发表文章@%s", mPostContent.getBoardEngName()));
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

    public class BytesContainer {
        public String filename;
        public byte[] bytes;

        public BytesContainer(String filename, byte[] bytes) {
            this.bytes = bytes;
            this.filename = filename;
        }
    }

    public void publishPost() {

        postPublishResult = SMTHHelper.AJAX_RESULT_OK;
        postPUblishMessage = "";

        final String progressHint = "发表文章中(%d/%d)...";
        ComposePostActivity.totalSteps = 1;
        ComposePostActivity.currentStep = 1;
        if(mPhotos != null){
            ComposePostActivity.totalSteps += mPhotos.size();
        }

        showProgress(String.format(progressHint, ComposePostActivity.currentStep, ComposePostActivity.totalSteps), true);
        Log.d(TAG, "publishPost: ");

        final SMTHHelper helper = SMTHHelper.getInstance();

        // update attachments
        Observable<AjaxResponse> resp1 = Observable.from(mPhotos)
                .map(new Func1<String, BytesContainer>() {
                    @Override
                    public BytesContainer call(String filename) {
                        byte[] bytes = SMTHHelper.getBitmapBytesWithResize(filename);
                        return new BytesContainer(filename, bytes);
                    }
                })
                .map(new Func1<BytesContainer, AjaxResponse>() {
                    @Override
                    public AjaxResponse call(BytesContainer container) {
                        RequestBody requestBody = RequestBody.create(MediaType.parse("image/jpeg"), container.bytes);
                        List<AjaxResponse> resps = helper.wService.uploadAttachment(mPostContent.getBoardEngName(), StringUtils.getLastStringSegment(container.filename), requestBody)
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
        Observable<AjaxResponse> resp2 = SMTHHelper.publishPost(mPostContent.getBoardEngName(), mTitle.getText().toString(), mContent.getText().toString(), "0", mPostContent.getPostid());


        // process all these tasks one by one
        Observable.concat(resp1, resp2)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<AjaxResponse>() {
                    @Override
                    public void onCompleted() {
                        showProgress(null, false);

                        String dialogTitle = "发表成功";
                        String dialogMessage = "结束编辑，或者停留在当前界面继续编辑？";
                        if(postPublishResult != SMTHHelper.AJAX_RESULT_OK) {
                            dialogTitle = "发表失败";
                            dialogMessage = "错误信息:\n" +postPUblishMessage + "\n" + dialogMessage;
                        }

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ComposePostActivity.this);
                        alertDialogBuilder.setTitle(dialogTitle)
                                .setMessage(dialogMessage)
                                .setCancelable(false)
                                .setPositiveButton("结束编辑", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // if this button is clicked, close current activity
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

                    @Override
                    public void onError(Throwable e) {
                        showProgress(null, false);
                        Log.d(TAG, "onError: " + Log.getStackTraceString(e));
                        Toast.makeText(ComposePostActivity.this, "发布失败!", Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onNext(AjaxResponse ajaxResponse) {
                        Log.d(TAG, "onNext: " + ajaxResponse.toString());
                        if(ajaxResponse.getAjax_st() != SMTHHelper.AJAX_RESULT_OK) {
                            postPublishResult = SMTHHelper.AJAX_RESULT_FAILED;
                            postPUblishMessage += ajaxResponse.getAjax_msg() + "\n";
                        }
                        ComposePostActivity.currentStep ++;
                        showProgress(String.format(progressHint, ComposePostActivity.currentStep, ComposePostActivity.totalSteps), true);
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
