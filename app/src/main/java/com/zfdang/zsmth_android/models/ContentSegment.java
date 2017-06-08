package com.zfdang.zsmth_android.models;

import android.text.Html;
import android.text.Spanned;

/**
 * Post content will finally be processed as a list of ContentSegment
 * It might represent one piece of text, or one attached image
 * Created by zfdang on 2016-3-31.
 */
public class ContentSegment {
  public static final int SEGMENT_TEXT = 292;
  public static final int SEGMENT_IMAGE = 165;

  private int type;
  private Spanned spanned;
  private String url;
  // image index among all attachments, it will be used for full screen image viewer
  private int imgIndex;

  public int getImgIndex() {
    return imgIndex;
  }

  public void setImgIndex(int imgIndex) {
    this.imgIndex = imgIndex;
  }

  public ContentSegment(int type, String content) {
    this.type = type;
    if (this.type == SEGMENT_TEXT) {
      this.setText(content);
    } else if (this.type == SEGMENT_IMAGE) {
      this.setUrl(content);
    }
  }

  public void setText(String text) {
    // http://stackoverflow.com/questions/4793347/jsoup-not-translating-ampersand-in-links-in-html
    // Html.fromHtml has bugs to parse &mid: only &mid; should be converted, but it convert &mid wrongly
    String tempText = text.replaceAll("&mid([^;])", "&amp;mid$1");
    this.spanned = Html.fromHtml(tempText);
  }

  public Spanned getSpanned() {
    return spanned;
  }

  public int getType() {
    return type;
  }

  public String getUrl() {
    return url;
  }

  public void setUrl(String url) {
    this.url = url;
  }
}
