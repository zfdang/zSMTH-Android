package com.zfdang.zsmth_android.helpers;

import android.text.Html;
import android.util.Log;
import com.zfdang.SMTHApplication;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * StringUtils. Created by zfdang on 2016-3-28.
 */
public class StringUtils {
  private static SimpleDateFormat dateformat = null;

  public static String getFormattedString(Date date) {
    if (dateformat == null) dateformat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    if (date != null) return dateformat.format(date);
    return "";
  }

  public static String subStringBetween(String line, String str1, String str2) {
    if (line == null || line.length() == 0) {
      return "";
    }

    int idx1 = line.indexOf(str1);
    int idx2 = line.lastIndexOf(str2);
    if (idx1 != -1 && idx2 != -1) {
      return line.substring(idx1 + str1.length(), idx2);
    }
    {
      return "";
    }
  }

  // [团购]3.28-4.03 花的传说饰品团购(18) ==> 18
  public static String getReplyCountInParentheses(String content) {
    Pattern hp = Pattern.compile("\\((\\d+)\\)$", Pattern.DOTALL);
    Matcher hm = hp.matcher(content);
    if (hm.find()) {
      String count = hm.group(1);
      return count;
    }

    return "";
  }

  // /nForum/board/ADAgent_TG ==> ADAgent_TG
  // /nForum/article/RealEstate/5017593 ==> 5017593
  public static String getLastStringSegment(String content) {
    if (content == null || content.length() == 0) {
      return "";
    }
    String[] segments = content.split("/");
    if (segments.length > 0) {
      return segments[segments.length - 1];
    }
    return "";
  }

  public static String lookupIPLocation(String content) {
    Pattern myipPattern = Pattern.compile("FROM[: ]*(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.)[\\d\\*]+");
    Matcher myipMatcher = myipPattern.matcher(content);
    while (myipMatcher.find()) {
      String ipl = myipMatcher.group(1);
      if (ipl.length() > 5) {
        ipl = "$1\\*(" + SMTHApplication.geoDB.getLocation(ipl + "1") + ")";
      } else {
        ipl = "$1\\*";
      }
      content = myipMatcher.replaceAll(ipl);
    }
    return content;
  }

  public static String lookupIPLocationInProfile(String content) {
    Pattern myipPattern = Pattern.compile("(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.)[\\d\\*]+");
    Matcher myipMatcher = myipPattern.matcher(content);
    while (myipMatcher.find()) {
      String ipl = myipMatcher.group(1);
      if (ipl.length() > 5) {
        ipl = "$1\\*(" + SMTHApplication.geoDB.getLocation(ipl + "1") + ")";
      } else {
        ipl = "$1\\*";
      }
      content = myipMatcher.replaceAll(ipl);
    }
    return content;
  }

  public static boolean isEmptyString(String content) {
    // non-empty if it's long enough
    if (content.length() > 20) {
      return false;
    }

    String text = Html.fromHtml(content).toString();
    for (int i = 0; i < text.length(); i++) {
      int value = Character.codePointAt(text, i);
      // http://www.utf8-chartable.de/unicode-utf8-table.pl?utf8=dec
      // 如果value不是控制符(0~31)，value不是SPACE(32)，不是"NO-BREAK SPACE"(160), 则认为是非空的
      if (value > 32 && value != 160) {
        return false;
      }
    }
    return true;
  }

  // ellipsize in the mid
  public static String getEllipsizedMidString(String content, int maxLen) {
    if (maxLen < 5) {
      maxLen = 5;
    }

    int length = content.length();
    if (length <= maxLen) {
      return content;
    }

    int pos1 = (maxLen - 3) / 2;
    int pos2 = length - (maxLen - 3 - pos1);

    String result = content.substring(0, pos1) + "..." + content.substring(pos2, length);

    Log.d("Ellipsize", "getEllipsizedMidString: " + content + "==>" + result);
    return result;
  }
}
