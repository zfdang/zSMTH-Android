package com.zfdang.zsmth_android;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import java.util.ArrayList;

import me.nereo.multi_image_selector.MultiImageSelectorActivity;

public class ComposePostActivity extends AppCompatActivity {

    private static final int REQUEST_CODE = 653;
    private static final String TAG = "ComposePostActivity";


    private Button mButton;
    private EditText mTitle;
    private EditText mAttachments;
    private EditText mContent;
    private ArrayList<String> mPhotos;
    private TextView mContentCount;

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




    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.compose_post_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int code = item.getItemId();
        if(code == android.R.id.home) {

        } else if (code == android.R.id.hint) {

        }
        return super.onOptionsItemSelected(item);
    }

}
