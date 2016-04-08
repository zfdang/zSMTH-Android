package com.bzh.mysimplefresco.lib;

import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.NinePatchDrawable;
import android.text.TextUtils;

import com.zfdang.zsmth_android.R;


/**
 * ========================================================== <br>
 * <b>版权</b>：　　　别志华 版权所有(c) 2015 <br>
 * <b>作者</b>：　　　别志华 biezhihua@163.com<br>
 * <b>创建日期</b>：　15-9-18 <br>
 * <b>描述</b>：　　　<br>
 * <b>版本</b>：　    V1.0 <br>
 * <b>修订历史</b>：　<br>
 * ========================================================== <br>
 */
public class MyNinePatchDrawable extends NinePatchDrawable {
    private String mStr;
    private Paint mPaint;

    public MyNinePatchDrawable(Resources res, Bitmap bitmap, byte[] chunk, Rect padding, String srcName, String str) {
        super(res, bitmap, chunk, padding, srcName);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setColor(Color.WHITE);
        mPaint.setStyle(Paint.Style.FILL);
        mPaint.setTextSize(res.getDimensionPixelSize(R.dimen.overlay_text_size_16));
        mStr = str;
    }


    @Override
    public void draw(Canvas canvas) {
        super.draw(canvas);
        if (TextUtils.isEmpty(mStr)) {
            return;
        }
        float textHeight = mPaint.descent() - mPaint.ascent();
        float textWidth = mPaint.measureText(mStr);

        float offsetX = textWidth / 2;
        float offsetY = textHeight / 2;

        float disY = getIntrinsicHeight() / 2 - offsetY;
        float disX = getIntrinsicWidth() / 2 - offsetX;

        canvas.drawText(mStr, this.getBounds().right - getIntrinsicWidth() + disX, this.getBounds().bottom - getIntrinsicHeight() + textHeight, mPaint);
    }
}
