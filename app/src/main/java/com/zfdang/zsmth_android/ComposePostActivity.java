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
import com.zfdang.zsmth_android.models.ComposePostContext;
import com.zfdang.zsmth_android.newsmth.SMTHHelper;

import java.util.ArrayList;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class ComposePostActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 653;
    private static final String TAG = "ComposePostActivity";

    private ProgressDialog pdialog = null;

    private Button mButton;
    private EditText mTitle;
    private EditText mAttachments;
    private EditText mContent;
    private ArrayList<String> mPhotos;
    private TextView mContentCount;

    private ComposePostContext mPostContent;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if(requestCode == REQUEST_CODE){
            if (resultCode == RESULT_OK && data != null) {
                mPhotos = data.getStringArrayListExtra(MultiImageSelectorActivity.EXTRA_RESULT);
                mAttachments.setText(String.format("共有%d个附件", mPhotos.size()));
//                for (String filename: mPhotos) {
//                    Log.d(TAG, "onActivityResult: " + filename);
//                }
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
                Intent intent = new Intent(ComposePostActivity.this, MultiImageSelectorActivity.class);
                // whether show camera
                intent.putExtra(MultiImageSelectorActivity.EXTRA_SHOW_CAMERA, true);
                // max select image amount
                intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_COUNT, 9);
                // select mode (MultiImageSelectorActivity.MODE_SINGLE OR MultiImageSelectorActivity.MODE_MULTI)
                intent.putExtra(MultiImageSelectorActivity.EXTRA_SELECT_MODE, MultiImageSelectorActivity.MODE_MULTI);
                // default select images (support array list)
                intent.putStringArrayListExtra(MultiImageSelectorActivity.EXTRA_DEFAULT_SELECTED_LIST, mPhotos);
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

        Log.d(TAG, "initFromIntent: " + mPostContent.toString());

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


    public void publishPost() {
        showProgress("发表文章中...", true);

        SMTHHelper helper = SMTHHelper.getInstance();
        SMTHHelper.publishPost(mPostContent.getBoardEngName(),
                mTitle.getText().toString(), mContent.getText().toString(),
                "0", mPostContent.getPostid())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>() {
                    @Override
                    public void onCompleted() {
                    }

                    @Override
                    public void onError(Throwable e) {
                        showProgress(null, false);
                        Log.d(TAG, Log.getStackTraceString(e));
                        Toast.makeText(ComposePostActivity.this, Log.getStackTraceString(e), Toast.LENGTH_LONG).show();
                    }

                    @Override
                    public void onNext(String s) {
                        showProgress(null, false);

                        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ComposePostActivity.this);
                        alertDialogBuilder.setTitle(s)
                                .setMessage("返回之前界面，或者停留在当前编辑界面？")
                                .setCancelable(false)
                                .setPositiveButton("返回", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // if this button is clicked, close current activity
                                        ComposePostActivity.this.finish();
                                    }
                                })
                                .setNegativeButton("停留", new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int id) {
                                        // if this button is clicked, just close the dialog box and do nothing
                                        dialog.cancel();
                                    }
                                }).create().show();
                    }
                });

    }

    public void onBackAction() {
        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(ComposePostActivity.this);
        alertDialogBuilder.setTitle("返回确认")
                .setMessage("返回之前界面，或者停留在当前界面继续编辑？")
                .setCancelable(false)
                .setPositiveButton("返回", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, close current activity
                        ComposePostActivity.this.finish();
                    }
                })
                .setNegativeButton("停留", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        // if this button is clicked, just close the dialog box and do nothing
                        dialog.cancel();
                    }
                }).create().show();
    }

}
