package com.zfdang.zsmth_android.helpers;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class FileSaveUtils {
    private static final String TAG = "FileSaveUtils";
    private static final String RELATIVE_PATH = Environment.DIRECTORY_PICTURES + File.separator + "zSMTH";

    public static void saveBitmapToGallery(Context context, Bitmap bitmap, String fileName) {
        OutputStream fos = null;
        Uri imageUri = null;
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = context.getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, RELATIVE_PATH);
                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                if (imageUri != null) {
                    fos = resolver.openOutputStream(imageUri);
                }
            } else {
                // Legacy storage
                String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        .toString() + File.separator + "zSMTH";
                File file = new File(imagesDir);
                if (!file.exists()) {
                    file.mkdirs();
                }
                File image = new File(imagesDir, fileName);
                fos = new FileOutputStream(image);
                // For legacy, we might need to scan the file
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(image)));
            }

            if (fos != null) {
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos);
                fos.flush();
                Toast.makeText(context, "图片已保存到相册", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "saveBitmapToGallery: ", e);
            Toast.makeText(context, "保存图片失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            try {
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void saveImageToGallery(Context context, File sourceFile, String fileName) {
        if (sourceFile == null || !sourceFile.exists())
            return;

        OutputStream fos = null;
        BufferedInputStream bis = null;
        Uri imageUri = null;

        try {
            bis = new BufferedInputStream(new FileInputStream(sourceFile));

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                ContentResolver resolver = context.getContentResolver();
                ContentValues contentValues = new ContentValues();
                contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, fileName);
                String mimeType = fileName.endsWith(".gif") ? "image/gif" : "image/jpeg";
                contentValues.put(MediaStore.MediaColumns.MIME_TYPE, mimeType);
                contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, RELATIVE_PATH);

                imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, contentValues);
                if (imageUri != null) {
                    fos = resolver.openOutputStream(imageUri);
                }
            } else {
                String imagesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
                        .toString() + File.separator + "zSMTH";
                File file = new File(imagesDir);
                if (!file.exists()) {
                    file.mkdirs();
                }
                File image = new File(imagesDir, fileName);
                fos = new FileOutputStream(image);
                // For legacy, we might need to scan the file
                context.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.fromFile(image)));
            }

            if (fos != null) {
                byte[] buffer = new byte[1024];
                int len;
                while ((len = bis.read(buffer)) != -1) {
                    fos.write(buffer, 0, len);
                }
                fos.flush();
                Toast.makeText(context, "图片已保存到相册", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Log.e(TAG, "saveImageToGallery: ", e);
            Toast.makeText(context, "保存图片失败: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        } finally {
            try {
                if (bis != null)
                    bis.close();
                if (fos != null)
                    fos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
