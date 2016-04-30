package com.zfdang.zsmth_android;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.PopupWindow;
import android.widget.RadioButton;

import com.wx.wheelview.widget.WheelView;
import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.models.Post;

/**
 * Created by zfdang on 2016-4-26.
 */
public class PopupForwardWindow extends PopupWindow {
    private static final String TAG = "PopupForwardWindow";

    Activity mContext;
    private OnForwardInterface mListener;
    private View contentView;
    private WheelView wheelView;
    private EditText etMessage;
    static public Post post;
    private RadioButton mTargetSelf;
    private RadioButton mTargetOther;
    private EditText mTargetOtherContent;
    private CheckBox mThread;
    private CheckBox mNoRef;
    private CheckBox mNoAtt;


    // http://stackoverflow.com/questions/23464232/how-would-you-create-a-popover-view-in-android-like-facebook-comments
    public void initPopupWindow(final Activity context, Post post) {
        mContext = context;
        this.post = post;
        if (context instanceof OnForwardInterface) {
            mListener = (OnForwardInterface) context;
        } else {
            Log.d(TAG, "initPopupWindow: " + "context does not implement SearchInterface");
        }

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView = layoutInflater.inflate(R.layout.popup_forward_layout, null, false);


        mTargetSelf = (RadioButton) contentView.findViewById(R.id.popup_forward_target_self);
        mTargetSelf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTargetSelf.isChecked()) {
                    mTargetOther.setChecked(false);
                    mTargetOtherContent.setEnabled(false);
                }
            }
        });
        mTargetOther = (RadioButton) contentView.findViewById(R.id.popup_forward_target_other);
        mTargetOther.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mTargetOther.isChecked()) {
                    mTargetSelf.setChecked(false);
                    mTargetOtherContent.setEnabled(true);
                }
            }
        });
        mTargetOtherContent = (EditText) contentView.findViewById(R.id.popup_forward_target_other_content);

        mThread = (CheckBox) contentView.findViewById(R.id.popup_forward_thread);
        mThread.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mThread.isChecked()) {
                    mNoRef.setEnabled(true);
                } else {
                    mNoRef.setEnabled(false);
                }
            }
        });
        mNoRef = (CheckBox) contentView.findViewById(R.id.popup_forward_noref);
        mNoAtt = (CheckBox) contentView.findViewById(R.id.popup_forward_noatt);

        // init status
        mTargetSelf.setChecked(true);
        mTargetOther.setChecked(false);
        mTargetOtherContent.setEnabled(false);
        mTargetOtherContent.setText(Settings.getInstance().getTarget());
        mThread.setChecked(false);
        mNoRef.setEnabled(false);

        Button cancel = (Button) contentView.findViewById(R.id.popup_forward_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        Button confirm = (Button) contentView.findViewById(R.id.popup_forward_confirm);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(mListener != null) {
                    String target = null;
                    if(SMTHApplication.activeUser != null) {
                        target = SMTHApplication.activeUser.getId();
                    }
                    if(mTargetOther.isChecked()) {
                        target = mTargetOtherContent.getText().toString().trim();
                        Settings.getInstance().setTarget(target);
                    }
                    mListener.OnForwardAction(PopupForwardWindow.post, target, mThread.isChecked(), mNoRef.isChecked(), mNoAtt.isChecked());
                }
                dismiss();
            }
        });

        // get device size
        Display display = context.getWindowManager().getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);

        this.setContentView(contentView);
        this.setWidth((int)(size.x * 0.95));
        this.setHeight((int)(size.y * 0.55));
        // http://stackoverflow.com/questions/12232724/popupwindow-dismiss-when-clicked-outside
        // this.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        this.setFocusable(true);
    }

    static public interface OnForwardInterface {
        public void OnForwardAction(Post post, String target, boolean threads, boolean noref, boolean noatt);
    }

}
