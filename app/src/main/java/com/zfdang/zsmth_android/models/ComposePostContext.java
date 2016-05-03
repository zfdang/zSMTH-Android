package com.zfdang.zsmth_android.models;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zfdang on 2016-4-4.
 */
public class ComposePostContext implements Parcelable {
    private String boardEngName;
    private String postTitle;
    private String postid;
    private String postContent;
    private String postAuthor;
    private boolean throughMail;

    public ComposePostContext() {
    }

    public boolean isThroughMail() {
        return throughMail;
    }

    public void setThroughMail(boolean throughMail) {
        this.throughMail = throughMail;
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

    public String getPostid() {
        return postid;
    }

    public void setPostid(String postid) {
        this.postid = postid;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.boardEngName);
        dest.writeString(this.postTitle);
        dest.writeString(this.postid);
        dest.writeString(this.postContent);
        dest.writeString(this.postAuthor);
        dest.writeByte(throughMail ? (byte) 1 : (byte) 0);
    }

    protected ComposePostContext(Parcel in) {
        this.boardEngName = in.readString();
        this.postTitle = in.readString();
        this.postid = in.readString();
        this.postContent = in.readString();
        this.postAuthor = in.readString();
        this.throughMail = in.readByte() != 0;
    }

    public static final Creator<ComposePostContext> CREATOR = new Creator<ComposePostContext>() {
        @Override
        public ComposePostContext createFromParcel(Parcel source) {
            return new ComposePostContext(source);
        }

        @Override
        public ComposePostContext[] newArray(int size) {
            return new ComposePostContext[size];
        }
    };

    @Override
    public String toString() {
        return "ComposePostContext{" +
                "boardEngName='" + boardEngName + '\'' +
                ", postTitle='" + postTitle + '\'' +
                ", postid='" + postid + '\'' +
                ", postContent='" + postContent + '\'' +
                ", postAuthor='" + postAuthor + '\'' +
                ", throughMail=" + throughMail +
                '}';
    }
}
