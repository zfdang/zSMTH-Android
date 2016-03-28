package com.zfdang.zsmth_android.helpers;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by zfdang on 2016-3-28.
 */
public class StringUtils {
    private static SimpleDateFormat dateformat = null;

    public static String getFormattedString(Date date){
        if(dateformat == null)
            dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        if(date != null)
            return dateformat.format(date);
        return "";
    }

    public static String subStringBetween(String line, String str1, String str2) {
        int idx1 = line.indexOf(str1);
        int idx2 = line.indexOf(str2);
        return line.substring(idx1 + str1.length(), idx2);
    }


    // /nForum/board/ADAgent_TG ==> ADAgent_TG
    // /nForum/article/RealEstate/5017593 ==> 5017593
    public static String getLastStringSegment(String content) {
        if(content == null || content.length() == 0){
            return "";
        }
        String[] segments = content.split("/");
        if(segments.length > 0) {
            return segments[segments.length - 1];
        }
        return "";
    }
}
