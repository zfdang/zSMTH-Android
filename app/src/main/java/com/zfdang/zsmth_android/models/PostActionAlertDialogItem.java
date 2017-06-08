package com.zfdang.zsmth_android.models;

/**
 * this class is used to build AlertDialog for long press on post item
 * Created by zfdang on 2016-3-28.
 */
public class PostActionAlertDialogItem {
  public final String text;
  public final int icon;

  public PostActionAlertDialogItem(String text, Integer icon) {
    this.text = text;
    this.icon = icon;
  }

  @Override public String toString() {
    return text;
  }
}
