package com.zfdang.zsmth_android.listeners;

/**
 * Created by zfdang on 2016-3-26.
 * https://gist.github.com/ssinss/e06f12ef66c51252563e
 */

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

public abstract class EndlessRecyclerOnScrollListener extends RecyclerView.OnScrollListener {
  public static String TAG = EndlessRecyclerOnScrollListener.class.getSimpleName();

  private int previousTotal = 0; // The total number of items in the dataset after the last load
  private boolean loading = false; // True if we are still waiting for the last set of data to load.
  private int visibleThreshold = 5; // The minimum amount of items to have below your current scroll position before loading more.
  int firstVisibleItem, visibleItemCount, totalItemCount;

  private int current_page = 1;

  private LinearLayoutManager mLinearLayoutManager;

  public EndlessRecyclerOnScrollListener(LinearLayoutManager linearLayoutManager) {
    this.mLinearLayoutManager = linearLayoutManager;
  }

  @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
    super.onScrolled(recyclerView, dx, dy);

    if (dy < 0) {
      return;
    }
    // check for scroll down only
    visibleItemCount = recyclerView.getChildCount();
    totalItemCount = mLinearLayoutManager.getItemCount();
    firstVisibleItem = mLinearLayoutManager.findFirstVisibleItemPosition();

    synchronized (this) {
      if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
        // End has been reached, Do something
        current_page++;
        onLoadMore(current_page);
        loading = true;
      }
    }
  }

  public void setLoading(boolean loading) {
    this.loading = loading;
  }

  public abstract void onLoadMore(int current_page);
}