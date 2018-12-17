package com.zfdang.zsmth_android.helpers;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.widget.Toast;
import com.klinker.android.link_builder.Link;
import com.zfdang.SMTHApplication;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by zfdang on 2016-5-14.
 */
public class ActivityUtils {
  private static final String TAG = "ActivityUtils";

  public static void openLink(String link, Activity activity) {
    Uri uri = Uri.parse(link);
    if (uri.getScheme() == null) {
      uri = Uri.parse("http://" + link);
    }
    Intent browserIntent = new Intent(Intent.ACTION_VIEW, uri);
    try {
      activity.startActivity(browserIntent);
    } catch (ActivityNotFoundException e) {
      Toast.makeText(activity, "链接打开错误:" + e.toString(), Toast.LENGTH_LONG).show();
    }
  }

  public static void sendEmail(String link, Activity activity) {
        /* Create the Intent */
    final Intent emailIntent = new Intent(android.content.Intent.ACTION_SEND);

        /* Fill it with Data */
    emailIntent.setType("plain/text");
    emailIntent.putExtra(android.content.Intent.EXTRA_EMAIL, new String[] { link });
    emailIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "");
    emailIntent.putExtra(android.content.Intent.EXTRA_TEXT, " \n \n \n \n--\n来自zSMTH的邮件\n");

    try {
      activity.startActivity(Intent.createChooser(emailIntent, "发邮件..."));
    } catch (ActivityNotFoundException e) {
      Toast.makeText(activity, "链接打开错误:" + e.toString(), Toast.LENGTH_LONG).show();
    }
  }

  // this will be called in PostRecyclerViewAdapter & MailContentActivity
  public static List<Link> getPostSupportedLinks(final Activity activity) {
    List<Link> links = new ArrayList<>();

    // web URL link
    Link weburl = new Link(Regex.WEB_URL_PATTERN);
    weburl.setTextColor(Color.parseColor("#00BCD4"));
    weburl.setHighlightAlpha(.4f);
    weburl.setOnClickListener(new Link.OnClickListener() {
      @Override public void onClick(String clickedText) {
        ActivityUtils.openLink(clickedText, activity);
      }
    });
    weburl.setOnLongClickListener(new Link.OnLongClickListener() {
      @Override public void onLongClick(String clickedText) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
          final android.content.ClipboardManager clipboardManager =
              (android.content.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
          final android.content.ClipData clipData = android.content.ClipData.newPlainText("PostContent", clickedText);
          clipboardManager.setPrimaryClip(clipData);
        } else {
          final android.text.ClipboardManager clipboardManager =
              (android.text.ClipboardManager) activity.getSystemService(Context.CLIPBOARD_SERVICE);
          clipboardManager.setText(clickedText);
        }
        Toast.makeText(SMTHApplication.getAppContext(), "链接已复制到剪贴板", Toast.LENGTH_SHORT).show();
      }
    });

    // email link
    Link emaillink = new Link(Regex.EMAIL_ADDRESS_PATTERN);
    emaillink.setTextColor(Color.parseColor("#00BCD4"));
    emaillink.setHighlightAlpha(.4f);
    emaillink.setOnClickListener(new Link.OnClickListener() {
      @Override public void onClick(String clickedText) {
        ActivityUtils.sendEmail(clickedText, activity);
      }
    });

    links.add(weburl);
    links.add(emaillink);

    return links;
  }

  // show application info page
  public static void showAppInfoPage(final Context context){
    AlertDialog.Builder builder;
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
      builder = new AlertDialog.Builder(context, android.R.style.Theme_Material_Dialog_Alert);
    } else {
      builder = new AlertDialog.Builder(context);
    }
    builder.setTitle("zSMTH需要文件读写权限")
            .setMessage("现在前往\"应用程序信息\"里设置zSMTH的权限么？")
            .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                // open app setting page
                Uri packageURI=Uri.parse("package:" + "com.zfdang.zsmth_android");
                Intent intent=new Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS,packageURI);
                context.startActivity(intent);
              }
            })
            .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
              public void onClick(DialogInterface dialog, int which) {
                // do nothing
              }
            })
            .setIcon(android.R.drawable.ic_dialog_alert)
            .show();

  }
}
