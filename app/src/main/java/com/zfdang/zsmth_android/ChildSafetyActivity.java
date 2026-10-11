package com.zfdang.zsmth_android;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.appcompat.app.AlertDialog;
import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.models.ComposePostContext;

/** Child safety information and reporting stay inside the client. */
public class ChildSafetyActivity extends SMTHBaseActivity {
  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_child_safety);
    setSupportActionBar(findViewById(R.id.toolbar));
    setTitle(R.string.child_safety_title);
    if (getSupportActionBar() != null) getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    findViewById(R.id.child_safety_report).setOnClickListener(view -> {
      new AlertDialog.Builder(this)
          .setMessage(R.string.child_safety_report_notice)
          .setPositiveButton(R.string.child_safety_report_action, (dialog, which) -> {
            ComposePostContext context = new ComposePostContext();
            context.setComposingMode(ComposePostContext.MODE_CHILD_SAFETY_REPORT);
            context.setPostAuthor("sysop");
            context.setPostContent("");
            Intent intent = new Intent(this, ComposePostActivity.class);
            intent.putExtra(SMTHApplication.COMPOSE_POST_CONTEXT, context);
            startActivity(intent);
          })
          .setNegativeButton(android.R.string.cancel, null).show();
    });
  }

  @Override public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == android.R.id.home) {
      finish();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }
}
