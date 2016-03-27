package com.zfdang.zsmth_android.models;

/**
 * Created by zfdang on 2016-3-14.
 */

import android.os.Parcel;
import android.os.Parcelable;

/**
 */
public class Topic implements Parcelable {

    private final int POST_PER_PAGE = 10;

    // 分隔符，只有一个category的名称
    public boolean isCategory;
    private String category;

    // 正常的主题
    private String boardEngName;
    private String boardChsName;

    private String topicID;
    private String title;
    private String author;
    private String publishDate;
    private String replier;
    private String replyDate;

    // 是否是置顶的主题
    public boolean isSticky;

    private String type;

    public int getTotalPageNo() {
        return totalPageNo;
    }

    private int totalPageNo;
    private int currentPageNo;
    private int totalPostNo;

    public void setTotalPostNo(int totalPostNo) {
        this.totalPostNo = totalPostNo;
        this.totalPageNo = this.totalPostNo / this.POST_PER_PAGE + 1;
    }


    // this is actually a category, used in guidance fragment to seperate hot topics
    public Topic(String category) {
        isCategory = true;
        this.category = category;
    }


    public Topic() {
        isCategory = false;
        this.boardChsName = "";
        this.boardEngName = "";
        this.author = "";
    }


    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getBoardEngName() {
        return boardEngName;
    }

    public void setBoardEngName(String boardEngName) {
        this.boardEngName = boardEngName;
    }

    public String getBoardChsName() {
        return boardChsName;
    }

    public void setBoardChsName(String boardChsName) {
        this.boardChsName = boardChsName;
    }

    public String getBoardName() {
        if (boardChsName != null && boardChsName.length() > 0) {
            return "[" + boardEngName + "]" + boardChsName;
        } else {
            return boardEngName;
        }
    }


    public String getTopicID() {
        return topicID;
    }

    public void setTopicID(String topicID) {
        this.topicID = topicID;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }


    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getReplier() {
        return replier;
    }

    public void setReplier(String replier) {
        this.replier = replier;
    }

    public String getReplyDate() {
        return replyDate;
    }

    public void setReplyDate(String replyDate) {
        this.replyDate = replyDate;
    }

    @Override
    public String toString() {
        if (isCategory) {
            return "Category " + this.category;
        } else {
            if (isSticky) {
                return String.format("置顶: (%s) %s by %s, %s @ %s", this.topicID, this.title, this.author, this.publishDate, this.boardEngName);
            } else {
                return String.format("(%s) %s by %s, %s @ %s", this.topicID, this.title, this.author, this.publishDate, this.boardEngName);

            }
        }
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
//        dest.writeInt(this.POST_PER_PAGE);
        dest.writeByte(isCategory ? (byte) 1 : (byte) 0);
        dest.writeString(this.category);
        dest.writeString(this.boardEngName);
        dest.writeString(this.boardChsName);
        dest.writeString(this.topicID);
        dest.writeString(this.title);
        dest.writeString(this.author);
        dest.writeString(this.publishDate);
        dest.writeString(this.replier);
        dest.writeString(this.replyDate);
        dest.writeByte(isSticky ? (byte) 1 : (byte) 0);
        dest.writeString(this.type);
        dest.writeInt(this.totalPageNo);
        dest.writeInt(this.currentPageNo);
        dest.writeInt(this.totalPostNo);
    }

    protected Topic(Parcel in) {
//        this.POST_PER_PAGE = in.readInt();
        this.isCategory = in.readByte() != 0;
        this.category = in.readString();
        this.boardEngName = in.readString();
        this.boardChsName = in.readString();
        this.topicID = in.readString();
        this.title = in.readString();
        this.author = in.readString();
        this.publishDate = in.readString();
        this.replier = in.readString();
        this.replyDate = in.readString();
        this.isSticky = in.readByte() != 0;
        this.type = in.readString();
        this.totalPageNo = in.readInt();
        this.currentPageNo = in.readInt();
        this.totalPostNo = in.readInt();
    }

    public static final Creator<Topic> CREATOR = new Creator<Topic>() {
        @Override
        public Topic createFromParcel(Parcel source) {
            return new Topic(source);
        }

        @Override
        public Topic[] newArray(int size) {
            return new Topic[size];
        }
    };
}