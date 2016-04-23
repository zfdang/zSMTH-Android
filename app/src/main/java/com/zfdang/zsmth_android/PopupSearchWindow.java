package com.zfdang.zsmth_android;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.PopupWindow;

import com.zfdang.multiple_images_selector.OnFolderRecyclerViewInteractionListener;

/**
 * Created by zfdang on 2016-4-23.
 */
public class PopupSearchWindow extends PopupWindow{
    private static final String TAG = "PopupSearchWindow";
    private Context mContext;
    private View conentView;
    private OnFolderRecyclerViewInteractionListener mListener = null;

    // http://stackoverflow.com/questions/23464232/how-would-you-create-a-popover-view-in-android-like-facebook-comments
    public void initPopupWindow(final Activity context) {
        mContext = context;
        if (context instanceof OnFolderRecyclerViewInteractionListener) {
            mListener = (OnFolderRecyclerViewInteractionListener) context;
        } else {
            Log.d(TAG, "initPopupWindow: " + "context does not implement OnFolderRecyclerViewInteractionListener");
        }

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        conentView = layoutInflater.inflate(R.layout.popup_topic_search, null, false);

        // get device size
        Display display = context.getWindowManager().getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);

        this.setContentView(conentView);
        this.setWidth((int)(size.x * 0.8));
        this.setHeight((int)(size.y * 0.4));
        // http://stackoverflow.com/questions/12232724/popupwindow-dismiss-when-clicked-outside
//        this.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.recyclerview_sticky_item_bg));
//        this.setOutsideTouchable(true);
        this.setFocusable(true);
    }

}
