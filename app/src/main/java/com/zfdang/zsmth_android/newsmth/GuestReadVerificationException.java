package com.zfdang.zsmth_android.newsmth;

import org.jsoup.nodes.Document;

/** The forum returned a verification form instead of the requested articles. */
public final class GuestReadVerificationException extends IllegalStateException {
  public GuestReadVerificationException() {
    super("水木社区要求游客完成阅读验证，请登录已有账号后重试。");
  }

  public static void check(Document document) {
    if (!document.select(".guest-read-challenge").isEmpty()) {
      throw new GuestReadVerificationException();
    }
  }
}
