package com.zfdang.zsmth_android.wxapi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;
import com.tencent.mm.opensdk.modelbase.BaseReq;
import com.tencent.mm.opensdk.modelbase.BaseResp;
import com.tencent.mm.opensdk.openapi.IWXAPI;
import com.tencent.mm.opensdk.openapi.IWXAPIEventHandler;
import com.zfdang.zsmth_android.WeChatShare;

public class WXEntryActivity extends Activity implements IWXAPIEventHandler {
  private IWXAPI api;

  @Override public void onCreate(Bundle state) {
    super.onCreate(state);
    api = WeChatShare.createApi(this);
    if (!api.handleIntent(getIntent(), this)) finish();
  }

  @Override protected void onNewIntent(Intent intent) {
    super.onNewIntent(intent);
    setIntent(intent);
    if (!api.handleIntent(intent, this)) finish();
  }

  @Override public void onReq(BaseReq request) { finish(); }

  @Override public void onResp(BaseResp response) {
    if (response.errCode == BaseResp.ErrCode.ERR_OK) {
      Toast.makeText(this, "微信分享成功", Toast.LENGTH_SHORT).show();
    } else if (response.errCode != BaseResp.ErrCode.ERR_USER_CANCEL) {
      Toast.makeText(this, "微信分享失败（" + response.errCode + "），请检查应用授权或重试",
          Toast.LENGTH_LONG).show();
    }
    finish();
  }

  @Override protected void onDestroy() {
    if (api != null) api.detach();
    super.onDestroy();
  }
}
