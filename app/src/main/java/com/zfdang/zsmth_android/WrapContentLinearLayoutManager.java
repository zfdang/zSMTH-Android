package com.zfdang.zsmth_android;

import android.content.Context;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Log;

/**
 * Created by zfdang on 2016-5-11.
 */
// There was actually a bug in RecyclerView and the support 23.1.1 still not fixed.
// http://stackoverflow.com/questions/31759171/recyclerview-and-java-lang-indexoutofboundsexception-inconsistency-detected-in
public class WrapContentLinearLayoutManager extends LinearLayoutManager {
  //... constructor
  public WrapContentLinearLayoutManager(Context context) {
    super(context);
  }

  @Override public void onLayoutChildren(RecyclerView.Recycler recycler, RecyclerView.State state) {
    try {
      super.onLayoutChildren(recycler, state);
    } catch (IndexOutOfBoundsException e) {
      Log.e("probe", "meet a IOOBE in RecyclerView");
    }
  }
}