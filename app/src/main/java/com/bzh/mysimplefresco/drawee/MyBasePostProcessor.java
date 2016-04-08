package com.bzh.mysimplefresco.drawee;

import android.graphics.Bitmap;
import android.graphics.BitmapRegionDecoder;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.Rect;

import com.facebook.common.references.CloseableReference;
import com.facebook.imageformat.ImageFormat;
import com.facebook.imagepipeline.bitmaps.PlatformBitmapFactory;
import com.facebook.imagepipeline.request.BasePostprocessor;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

/**
 * ========================================================== <br>
 * <b>版权</b>：　　　别志华 版权所有(c) 2015 <br>
 * <b>作者</b>：　　　别志华 biezhihua@163.com<br>
 * <b>创建日期</b>：　15-9-23 <br>
 * <b>描述</b>：　　　用于处理图片的类，在图片尚未传给process之前处理<br>
 * <b>版本</b>：　    V1.0 <br>
 * <b>修订历史</b>：　<br>
 * ========================================================== <br>
 */
public class MyBasePostProcessor extends BasePostprocessor {

    Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

    MySimpleDraweeView draweeView;

    public MyBasePostProcessor(MySimpleDraweeView draweeView) {
        this.draweeView = draweeView;
    }

    @Override
    public CloseableReference<Bitmap> process(Bitmap sourceBitmap, PlatformBitmapFactory bitmapFactory) {

        // 默认宽高比例显示 W:H = 1:2
        // 按照宽高比例截取图片区域
        if (sourceBitmap.getHeight() > (int) (sourceBitmap.getWidth() * MySimpleDraweeView.DEF_RATIO)) {
            Bitmap bitmap = decodeRegion(sourceBitmap, sourceBitmap.getWidth(), (int) (sourceBitmap.getWidth() * MySimpleDraweeView.DEF_RATIO));
            return super.process(bitmap, bitmapFactory);
        }

        // 将PNG图片转换成JPG，并将背景色设置为指定颜色
        else if (ImageFormat.PNG.equals(draweeView.getImageFormat()) && draweeView.isReplacePNGBackground() != -1) {
            replaceTransparent2TargetColor(sourceBitmap, draweeView.isReplacePNGBackground());
        }

        // PNG图片，并且设置了图片最大宽高，如果加载的PNG图片宽高超过指定宽高，并截取指定大小
        else if (ImageFormat.PNG.equals(draweeView.getImageFormat())
                && draweeView.getTargetImageSize() != -1
                && (sourceBitmap.getWidth() > draweeView.getTargetImageSize() || sourceBitmap.getHeight() > draweeView.getTargetImageSize())) {

            // 压缩图片
            Bitmap bitmap = Utils.decodeSampledBitmapFromByteArray(
                    bitmap2Bytes(sourceBitmap, 100),
                    draweeView.getTargetImageSize(),
                    draweeView.getTargetImageSize());

            // 截取图片
            Bitmap region = decodeRegion(bitmap, draweeView.getTargetImageSize(), draweeView.getTargetImageSize());
            bitmap.recycle();

            return super.process(region, bitmapFactory);
        }
        return super.process(sourceBitmap, bitmapFactory);
    }

    private void replaceTransparent2TargetColor(Bitmap sourceBitmap, int color) {
        Canvas canvas = new Canvas(sourceBitmap);
        canvas.drawColor(color, PorterDuff.Mode.DST_OVER);
        canvas.drawBitmap(sourceBitmap, 0, 0, mPaint);
    }

    /**
     * 将bitmap转化为数组
     */
    public static byte[] bitmap2Bytes(Bitmap bitmap, int quality) {
        if (bitmap == null) {
            throw new IllegalArgumentException("bitmap is not null");
        }
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, baos);
        return baos.toByteArray();
    }

    /**
     * 截取bitmap指定的宽高
     */
    public static Bitmap decodeRegion(Bitmap bitmap, int width, int height) {
        return decodeRegion(bitmap2Bytes(bitmap, 100), width, height);
    }

    public static Bitmap decodeRegion(byte[] bytes, int width, int height) {
        BitmapRegionDecoder bitmapRegionDecoder = null;
        try {
            bitmapRegionDecoder = BitmapRegionDecoder.newInstance(bytes, 0, bytes.length, true);
        } catch (IOException e) {
            e.printStackTrace();
        }
        Rect rect = new Rect(0, 0, width, height);
        assert bitmapRegionDecoder != null;
        return bitmapRegionDecoder.decodeRegion(rect, null);
    }
}
