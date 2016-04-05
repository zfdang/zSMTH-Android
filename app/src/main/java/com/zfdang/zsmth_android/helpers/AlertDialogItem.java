package com.zfdang.zsmth_android.helpers;

/**
 * this class is used to build AlertDialog for long press on post item
 * Created by zfdang on 2016-3-28.
 */
public class AlertDialogItem {
    public final String text;
    public final int icon;
    public AlertDialogItem(String text, Integer icon) {
        this.text = text;
        this.icon = icon;
    }
    @Override
    public String toString() {
        return text;
    }
}
