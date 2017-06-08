package com.zfdang.zsmth_android.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zfdang on 2016-4-4.
 */
public class ComposePostContext implements Parcelable {
  public static int MODE_NEW_POST = 1;
  public static int MODE_REPLY_POST = 2;  // two entries: 1. reply post in post list; 2. reply reffered post in mailbox
  public static int MODE_EDIT_POST = 3;
  public static int MODE_NEW_MAIL = 4;
  public static int MODE_NEW_MAIL_TO_USER = 5;  // new mail in user query interface
  public static int MODE_REPLY_MAIL = 6;  // two entries: 1. reply mail in mail box 2. reply post via mail in post list

  private String boardEngName;
  private String postTitle;
  private String postId;
  private String postContent;
  private String postAuthor;

  public int getComposingMode() {
    return composingMode;
  }

  public void setComposingMode(int composingMode) {
    this.composingMode = composingMode;
  }

  private int composingMode;

  public ComposePostContext() {
  }

  public boolean isValidPost() {
    return postTitle != null && postTitle.length() > 0;
  }

  public String getBoardEngName() {
    return boardEngName;
  }

  public void setBoardEngName(String boardEngName) {
    this.boardEngName = boardEngName;
  }

  public String getPostContent() {
    return postContent;
  }

  public void setPostContent(String postContent) {
    this.postContent = postContent;
  }

  public String getPostId() {
    return postId;
  }

  public void setPostId(String postId) {
    this.postId = postId;
  }

  public String getPostTitle() {
    return postTitle;
  }

  public void setPostTitle(String postTitle) {
    this.postTitle = postTitle;
  }

  public String getPostAuthor() {
    return postAuthor;
  }

  public void setPostAuthor(String postAuthor) {
    this.postAuthor = postAuthor;
  }

  @Override public String toString() {
    return "ComposePostContext{"
        + "boardEngName='"
        + boardEngName
        + '\''
        + ", postTitle='"
        + postTitle
        + '\''
        + ", postId='"
        + postId
        + '\''
        + ", postContent='"
        + postContent
        + '\''
        + ", postAuthor='"
        + postAuthor
        + '\''
        + ", composingMode="
        + composingMode
        + '}';
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.boardEngName);
    dest.writeString(this.postTitle);
    dest.writeString(this.postId);
    dest.writeString(this.postContent);
    dest.writeString(this.postAuthor);
    dest.writeInt(this.composingMode);
  }

  protected ComposePostContext(Parcel in) {
    this.boardEngName = in.readString();
    this.postTitle = in.readString();
    this.postId = in.readString();
    this.postContent = in.readString();
    this.postAuthor = in.readString();
    this.composingMode = in.readInt();
  }

  public static final Creator<ComposePostContext> CREATOR = new Creator<ComposePostContext>() {
    @Override public ComposePostContext createFromParcel(Parcel source) {
      return new ComposePostContext(source);
    }

    @Override public ComposePostContext[] newArray(int size) {
      return new ComposePostContext[size];
    }
  };
}
