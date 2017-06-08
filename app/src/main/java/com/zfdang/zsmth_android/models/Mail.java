package com.zfdang.zsmth_android.models;

import android.os.Parcel;
import android.os.Parcelable;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zfdang on 2016-3-15.
 */
public class Mail implements Parcelable {
  public String url;
  public String title;
  public String author;
  public String date;
  public boolean isNew;

  public boolean isCategory;
  public String category;

  // the following two fields are for refer posts
  public String fromBoard;
  public String referIndex;

  public Mail(String categoryName) {
    isCategory = true;
    category = categoryName;
  }

  public Mail() {
    isCategory = false;
  }

  public String getFrom() {
    String result = author;
    if (fromBoard != null && fromBoard.length() > 0) {
      result += String.format(" @ [%s]", fromBoard);
    }
    return result;
  }

  public String getMailIDFromURL() {
    if (url != null && url.length() > 0) {
      // '/nForum/mail/inbox/192.json'
      Pattern pattern = Pattern.compile("(\\d+)", Pattern.DOTALL);
      Matcher matcher = pattern.matcher(url);
      if (matcher.find()) {
        String mailid = matcher.group(0);
        return mailid;
      }
    }
    return "";
  }

  public boolean isRefferedPost() {
    return this.fromBoard != null && this.fromBoard.length() > 0;
  }

  @Override public String toString() {
    return "Mail{"
        + "author='"
        + author
        + '\''
        + ", url='"
        + url
        + '\''
        + ", title='"
        + title
        + '\''
        + ", date='"
        + date
        + '\''
        + ", isNew="
        + isNew
        + ", isCategory="
        + isCategory
        + ", category='"
        + category
        + '\''
        + ", fromBoard='"
        + fromBoard
        + '\''
        + ", referIndex="
        + referIndex
        + '}';
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.url);
    dest.writeString(this.title);
    dest.writeString(this.author);
    dest.writeString(this.date);
    dest.writeByte(isNew ? (byte) 1 : (byte) 0);
    dest.writeByte(isCategory ? (byte) 1 : (byte) 0);
    dest.writeString(this.category);
    dest.writeString(this.fromBoard);
    dest.writeString(this.referIndex);
  }

  protected Mail(Parcel in) {
    this.url = in.readString();
    this.title = in.readString();
    this.author = in.readString();
    this.date = in.readString();
    this.isNew = in.readByte() != 0;
    this.isCategory = in.readByte() != 0;
    this.category = in.readString();
    this.fromBoard = in.readString();
    this.referIndex = in.readString();
  }

  public static final Creator<Mail> CREATOR = new Creator<Mail>() {
    @Override public Mail createFromParcel(Parcel source) {
      return new Mail(source);
    }

    @Override public Mail[] newArray(int size) {
      return new Mail[size];
    }
  };
}