package com.zfdang.zsmth_android.helpers;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

// https://github.com/jgilfelt/android-sqlite-asset-helper
public class GEODatabase extends SQLiteAssetHelper {

  private static final String DATABASE_NAME = "qqwry.db";
  private static final int DATABASE_VERSION = 32; // updated to 2024-09-25, last version of dat format

  public GEODatabase(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);

    // you can use an alternate constructor to specify a database location
    // (such as a folder on the sd card)
    // you must ensure that this folder is available and you have permission
    // to write to it
    //super(context, DATABASE_NAME, context.getExternalFilesDir(null).getAbsolutePath(), null, DATABASE_VERSION);
    setForcedUpgrade();

    //        Log.d("GEODatabase", String.format("IP=166.111.8.28 GEO=%s", getLocation("166.111.8.1")));
    //        Log.d("GEODatabase", String.format("IP=59.66.211.1 GEO=%s", getLocation("59.66.211.1")));
  }

  private String Dot2LongIP(String dottedIP) {
    dottedIP = dottedIP.replace('*', '1');
    String[] addrArray = dottedIP.split("\\.");
    long int_max = 2147483647;

    long num = 0;
    for (int i = 0; i < addrArray.length; i++) {
      int power = 3 - i;
      num += ((Integer.parseInt(addrArray[i]) % 256) * Math.pow(256, power));
    }
    if (num < int_max) {
      return String.valueOf(num);
    } else {
      return String.valueOf(num - int_max - int_max - 2);
    }
  }

  public String getLocation(String dottedIP) {
    String intIP = Dot2LongIP(dottedIP);

    SQLiteDatabase db = getReadableDatabase();
    Cursor result = db.rawQuery("select ip,country from qqwry where ip< " + intIP + " order by ip desc limit 1", null);
    result.moveToFirst();
    String country = result.getString(1);
    result.close();

    return country;
  }
}