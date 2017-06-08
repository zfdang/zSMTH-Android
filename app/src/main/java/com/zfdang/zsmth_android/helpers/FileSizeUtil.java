package com.zfdang.zsmth_android.helpers;

import android.util.Log;
import java.io.File;
import java.io.FileInputStream;
import java.text.DecimalFormat;

/**
 * Created by zfdang on 2016-4-9.
 */
public class FileSizeUtil {
  public static final int SIZETYPE_B = 1;//获取文件大小单位为B的double值
  public static final int SIZETYPE_KB = 2;//获取文件大小单位为KB的double值
  public static final int SIZETYPE_MB = 3;//获取文件大小单位为MB的double值
  public static final int SIZETYPE_GB = 4;//获取文件大小单位为GB的double值

  private static final String TAG = "FileSizeUtil";

  /**
   * 获取文件指定文件的指定单位的大小
   *
   * @param filePath 文件路径
   * @param sizeType 获取大小的类型1为B、2为KB、3为MB、4为GB
   * @return double值的大小
   */
  public static double getFileOrFolderSize(String filePath, int sizeType) {
    File file = new File(filePath);
    long blockSize = 0;
    try {
      if (file.isDirectory()) {
        blockSize = getFolderSize(file);
      } else {
        blockSize = getFileSize(file);
      }
    } catch (Exception e) {
      Log.e(TAG, "getFileOrFolderSize: " + Log.getStackTraceString(e));
    }
    return FormatFileSize(blockSize, sizeType);
  }

  /**
   * 调用此方法自动计算指定文件或指定文件夹的大小
   *
   * @param filePath 文件路径
   * @return 计算好的带B、KB、MB、GB的字符串
   */
  public static String getAutoFileOrFolderSize(String filePath) {
    File file = new File(filePath);
    long blockSize = 0;
    try {
      if (file.isDirectory()) {
        blockSize = getFolderSize(file);
      } else {
        blockSize = getFileSize(file);
      }
    } catch (Exception e) {
      Log.e(TAG, "getFileOrFolderSize: " + Log.getStackTraceString(e));
    }
    return FormatFileSize(blockSize);
  }

  /**
   * 获取指定文件大小
   *
   * @throws Exception
   */
  public static long getFileSize(File file) throws Exception {
    long size = 0;
    if (file.exists()) {
      FileInputStream fis = null;
      try {
        fis = new FileInputStream(file);
        size = fis.available();
      } catch (Exception e) {
        Log.e(TAG, "getFileSize: " + Log.getStackTraceString(e));
      } finally {
        if (fis != null) {
          fis.close();
        }
      }
    }
    return size;
  }

  /**
   * 获取指定文件夹
   *
   * @throws Exception
   */
  public static long getFolderSize(File f) throws Exception {
    long size = 0;
    File flist[] = f.listFiles();
    for (int i = 0; i < flist.length; i++) {
      if (flist[i].isDirectory()) {
        size = size + getFolderSize(flist[i]);
      } else {
        size = size + getFileSize(flist[i]);
      }
    }
    return size;
  }

  /**
   * 转换文件大小
   */
  public static String FormatFileSize(long fileSize) {
    DecimalFormat df = new DecimalFormat("#.00");
    String formattedSize = "";
    if (fileSize == 0) {
      return "0B";
    }
    if (fileSize < 1024) {
      formattedSize = df.format((double) fileSize) + "B";
    } else if (fileSize < 1048576) {
      formattedSize = df.format((double) fileSize / 1024) + "KB";
    } else if (fileSize < 1073741824) {
      formattedSize = df.format((double) fileSize / 1048576) + "MB";
    } else {
      formattedSize = df.format((double) fileSize / 1073741824) + "GB";
    }
    return formattedSize;
  }

  /**
   * 转换文件大小,指定转换的类型
   */
  private static double FormatFileSize(long fileSize, int sizeType) {
    DecimalFormat df = new DecimalFormat("#.00");
    double fileSizeLong = 0;
    switch (sizeType) {
      case SIZETYPE_B:
        fileSizeLong = Double.valueOf(df.format((double) fileSize));
        break;
      case SIZETYPE_KB:
        fileSizeLong = Double.valueOf(df.format((double) fileSize / 1024));
        break;
      case SIZETYPE_MB:
        fileSizeLong = Double.valueOf(df.format((double) fileSize / 1048576));
        break;
      case SIZETYPE_GB:
        fileSizeLong = Double.valueOf(df.format((double) fileSize / 1073741824));
        break;
      default:
        break;
    }
    return fileSizeLong;
  }
}