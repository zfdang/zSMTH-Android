package com.bzh.mysimplefresco.drawee;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * ========================================================== <br>
 * <b>版权</b>：　　　别志华 版权所有(c) 2015 <br>
 * <b>作者</b>：　　　别志华 biezhihua@163.com<br>
 * <b>创建日期</b>：　15-9-26 <br>
 * <b>描述</b>：　　　<br>
 * <b>版本</b>：　   V1.0 <br>
 * <b>修订历史</b>：　<br>
 * ========================================================== <br>
 */
public class Utils {
    public static File getCopyFile(File file) {
        File copyFile = new File(Environment.getExternalStorageDirectory() + File.separator + getMD5(file.getName()));
        if (!copyFile.exists()) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            Bitmap bitmap = null;
            try {
                bitmap = BitmapFactory.decodeStream(new FileInputStream(file), null, options);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            Bitmap region = MyBasePostProcessor.decodeRegion(bitmap, options.outWidth, options.outHeight);
            byte[] bytes = MyBasePostProcessor.bitmap2Bytes(region, 80);
            FileOutputStream fo;
            try {
                fo = new FileOutputStream(copyFile);
                fo.write(bytes);
                fo.flush();
                fo.close();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return copyFile;
    }

    private static final String STR_EMPTY = "";
    private static final String STR_MD5 = "MD5";

    public static String getMD5(String content) {
        if (content != null && !"".equals(content)) {
            try {
                MessageDigest digest = MessageDigest.getInstance(STR_MD5);
                digest.update(content.getBytes());
                return getHashString(digest);

            } catch (NoSuchAlgorithmException e) {
                Log.e("yyt", e.getMessage());
            }
            return STR_EMPTY;
        }
        return "";
    }

    private static String getHashString(MessageDigest digest) {
        StringBuilder builder = new StringBuilder(STR_EMPTY);
        for (byte b : digest.digest()) {
            builder.append(Integer.toHexString((b >> 4) & 0xf));
            builder.append(Integer.toHexString(b & 0xf));
        }
        return builder.toString();
    }

    public static Bitmap decodeSampledBitmapFromByteArray(byte[] datas, int reqWidth, int reqHeight) {
        long startTime = System.currentTimeMillis();
        final BitmapFactory.Options options = new BitmapFactory.Options();
        // 当inJustDecodeBounds设为true时,不会加载图片仅获取图片尺寸信息
        options.inJustDecodeBounds = true;
        // 此时仅会将图片信息会保存至options对象内,decode方法不会返回bitmap对象
        BitmapFactory.decodeByteArray(datas, 0, datas.length, options);

        // 计算压缩比例,如inSampleSize=4时,图片会压缩成原图的1/4
        options.inSampleSize = calculateInSampleSizeGetSmall(options, reqWidth, reqHeight);

        // 当inJustDecodeBounds设为false时,BitmapFactory.decode...就会返回图片对象了
        options.inJustDecodeBounds = false;
        // 利用计算的比例值获取压缩后的图片对象
        options.inPreferredConfig = Bitmap.Config.ARGB_8888;
        options.inPurgeable = true;
        options.inInputShareable = true;
        Bitmap bitmap = BitmapFactory.decodeByteArray(datas, 0, datas.length, options);
        return bitmap;
    }


    public static int calculateInSampleSizeGetSmall(BitmapFactory.Options options, int reqWidth, int reqHeight) {
        // 源图片的高度和宽度
        final int height = options.outHeight;
        final int width = options.outWidth;
        int inSampleSize = 1;
        if (height > reqHeight || width > reqWidth) {
            // 计算出实际宽高和目标宽高的比率
            final int heightRatio = Math.round((float) height / (float) reqHeight);
            final int widthRatio = Math.round((float) width / (float) reqWidth);
            // 选择宽和高中最小的比率作为inSampleSize的值，这样可以保证最终图片的宽和高
            // 一定都会大于等于目标的宽和高。
            inSampleSize = heightRatio < widthRatio ? widthRatio : heightRatio;
        }
        return inSampleSize;
    }

    public static int getScreenWidth(Context context) {
        if (null != context) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            return metrics.widthPixels;
        }
        return 0;
    }

    public static int getScreenHeight(Context context) {
        if (null != context) {
            DisplayMetrics metrics = context.getResources().getDisplayMetrics();
            return metrics.heightPixels;
        }
        return 0;
    }


    public static int dip2px(Context context, int dipValue) {
        float reSize = context.getResources().getDisplayMetrics().density;
        return (int) ((dipValue * reSize) + 0.5);
    }

}
