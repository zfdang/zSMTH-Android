package com.zfdang.zsmth_android.helpers;

import android.support.v7.widget.RecyclerView;
import android.view.KeyEvent;

/**
 * Created by zfdang on 2016-3-28.
 */
public class RecyclerViewUtil {
  /*
   * Scroll the RecyclerView by VOLUME DOWN/UP
   * return true if RecyclerView was scrolled
   * otherwise return false
   */
  public static boolean ScrollRecyclerViewByKey(RecyclerView rv, int keyCode) {
    if (rv != null) {
      int offset = (int) (rv.getHeight() * 0.80);
      //            Log.d("RecyclerViewUtil", "offset is " + rv.getHeight());
      if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
        rv.smoothScrollBy(0, offset);
        return true;
      } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
        rv.smoothScrollBy(0, -offset);
        return true;
      }
    }
    return false;
  }
}
