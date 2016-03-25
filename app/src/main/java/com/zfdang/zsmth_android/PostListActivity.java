package com.zfdang.zsmth_android;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;

import com.zfdang.zsmth_android.models.PostListContent;

/**
 * An activity representing a single Topic detail screen. This
 * activity is only used narrow width devices. On tablet-size devices,
 * item details are presented side-by-side with a list of items
 * in a {@link BoardTopicActivity}.
 */
public class PostListActivity extends AppCompatActivity {

    private static final String TAG = "PostListActivity";
    private RecyclerView mRecyclerView = null;
    private String mSubjectID = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post_list);
        Toolbar toolbar = (Toolbar) findViewById(R.id.post_list_toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.post_list_fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own detail action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        // Show the Up button in the action bar.
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        mRecyclerView = (RecyclerView) findViewById(R.id.post_list);
        assert mRecyclerView != null;
        mRecyclerView.setAdapter(new PostRecyclerViewAdapter(PostListContent.ITEMS, null));

        // get Board information from launcher
        Intent intent = getIntent();
        mSubjectID = intent.getStringExtra("board_chs_name");
//        mSource = intent.getStringExtra("source");
//        String engName = intent.getStringExtra("board_eng_name");
//        if(engName != mBoardEngName) {
//            TopicListContent.clearBoardTopics();
//            mBoardEngName = engName;
//            TopicListContent.setBoardName(mBoardEngName);
//        }


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
}
