package com.zfdang.zsmth_android;

import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.PopupWindow;
import com.wx.wheelview.adapter.ArrayWheelAdapter;
import com.wx.wheelview.widget.WheelView;
import com.zfdang.zsmth_android.models.Post;

import java.util.Arrays;

public class PopupBanWindow extends PopupWindow {
    private static final String TAG = "PopupBanWindow";

    Activity mContext;
    private OnBanIDInterface mListener;
    private View contentView;
    private WheelView wheelView;
    private EditText banReason;
    private EditText banDay;

    static public Post post;

    // http://stackoverflow.com/questions/23464232/how-would-you-create-a-popover-view-in-android-like-facebook-comments
    public void initPopupWindow(final Activity context, Post post) {
        mContext = context;
        if (context instanceof OnBanIDInterface) {
            mListener = (OnBanIDInterface) context;
        } else {
            Log.d(TAG, "initPopupWindow: " + "context does not implement OnBanIDInterface");
        }

        PopupBanWindow.post = post;

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        contentView = layoutInflater.inflate(R.layout.popup_ban_layout, null, false);


        String[] scores = { "灌水", "干扰版面讨论秩序", "言语粗俗", "人身攻击", "开启无关话题", "发表不恰当文章",
                "未经许可的版面商业行为", "Test(仅供测试时使用)"};
        wheelView = (WheelView) contentView.findViewById(R.id.ban_reason_select);
        wheelView.setWheelAdapter(new ArrayWheelAdapter(mContext)); // 文本数据源
        wheelView.setSkin(WheelView.Skin.Common); // common皮肤
        wheelView.setWheelData(Arrays.asList(scores));  // 数据集合
        wheelView.setSelection(5);

        banReason = (EditText) contentView.findViewById(R.id.ban_reason);

        banDay = (EditText) contentView.findViewById(R.id.ban_day);

        wheelView.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int position, String s) {
                banReason.setText(s);
            }
        });

        banReason = (EditText) contentView.findViewById(R.id.ban_reason);

        banDay = (EditText) contentView.findViewById(R.id.ban_day);

        Button cancel = (Button) contentView.findViewById(R.id.ban_cancel);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                dismiss();
            }
        });

        Button confirm = (Button) contentView.findViewById(R.id.ban_submit);
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (mListener != null) {
                    mListener.OnBanIDAction(PopupBanWindow.post, banReason.getText().toString(),
                            Integer.parseInt(banDay.getText().toString()));
                }
                dismiss();
            }
        });

        // get device size
        Display display = context.getWindowManager().getDefaultDisplay();
        final Point size = new Point();
        display.getSize(size);

        this.setContentView(contentView);
        this.setWidth((int) (size.x * 0.8));
        this.setHeight((int) (size.y * 0.65));
        // http://stackoverflow.com/questions/12232724/popupwindow-dismiss-when-clicked-outside
        // this.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        // this.setOutsideTouchable(true);
        this.setFocusable(true);
    }


    public interface OnBanIDInterface {
        void OnBanIDAction(Post post, String banReason, Integer day);
    }

}
