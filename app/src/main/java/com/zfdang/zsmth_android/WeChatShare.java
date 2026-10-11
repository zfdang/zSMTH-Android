package com.zfdang.zsmth_android;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.widget.Toast;
import androidx.appcompat.content.res.AppCompatResources;
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX;
import com.tencent.mm.opensdk.modelmsg.WXMediaMessage;
import com.tencent.mm.opensdk.modelmsg.WXWebpageObject;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.WXAPIFactory;

/** Creates WeChat only for an explicit share or its callback. */
public final class WeChatShare {
  private static final String APP_ID = "wxa0a604767238b433";

  private WeChatShare() {}

  public static IWXAPI createApi(Context context) {
    IWXAPI api = WXAPIFactory.createWXAPI(context.getApplicationContext(), APP_ID, true);
    api.registerApp(APP_ID);
    return api;
  }

  public static void share(Activity activity, String title, String summary, String url,
                           boolean timeline) {
    IWXAPI api = createApi(activity);
    try {
      if (!api.isWXAppInstalled()) {
        Toast.makeText(activity, "未安装微信，请选择短信或邮件分享", Toast.LENGTH_LONG).show();
        return;
      }
      WXMediaMessage message = new WXMediaMessage(new WXWebpageObject(url));
      message.title = title.length() > 512 ? title.substring(0, 512) : title;
      message.description = summary;
      Drawable icon = AppCompatResources.getDrawable(activity, R.drawable.ic_launcher);
      if (icon != null) {
        Bitmap bitmap = Bitmap.createBitmap(96, 96, Bitmap.Config.ARGB_8888);
        icon.setBounds(0, 0, 96, 96);
        icon.draw(new Canvas(bitmap));
        message.setThumbImage(bitmap);
        bitmap.recycle();
      }
      SendMessageToWX.Req request = new SendMessageToWX.Req();
      request.transaction = "webpage" + java.util.UUID.randomUUID();
      request.message = message;
      request.scene = timeline ? SendMessageToWX.Req.WXSceneTimeline
          : SendMessageToWX.Req.WXSceneSession;
      if (!request.checkArgs() || !api.sendReq(request)) {
        Toast.makeText(activity, "无法发起微信分享，请检查微信版本或选择短信、邮件", Toast.LENGTH_LONG).show();
      }
    } finally {
      api.detach();
    }
  }
}
