package com.zfdang.zsmth_android.services;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

/**
 * Created by zfdang on 2016-4-6.
 */
@SuppressLint("ParcelCreator") public class UserStatusReceiver extends ResultReceiver {

  private Receiver receiver;

  public UserStatusReceiver(Handler handler) {
    super(handler);
  }

  public void setReceiver(Receiver receiver) {
    this.receiver = receiver;
  }

  @Override protected void onReceiveResult(int resultCode, Bundle resultData) {
    if (receiver != null) {
      receiver.onReceiveResult(resultCode, resultData);
    }
  }

  public void onServiceFailed() {
    receiver.onServerFailed();
  }

  public interface Receiver {
    void onReceiveResult(int resultCode, Bundle resultData);
    void onServerFailed();
  }
}
