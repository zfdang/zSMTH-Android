package com.zfdang.zsmth_android.models;

/**
 * Created by zfdang on 2016-3-14.
 */

import java.io.Serializable;

/**
 */
public class Topic implements Serializable {

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

    private int totalPageNo;
    private int currentPageNo;

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

    // this is a real hot topic
    public Topic(String boardChsName, String boardEngName, String title, String author) {
        this.boardChsName = boardChsName;
        this.boardEngName = boardEngName;
        this.title = title;
        this.author = author;
        isCategory = false;
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
            if(isSticky) {
                return String.format("置顶: (%s) %s by %s, %s @ %s", this.topicID, this.title, this.author, this.publishDate, this.boardEngName);
            } else {
                return String.format("(%s) %s by %s, %s @ %s", this.topicID, this.title, this.author, this.publishDate, this.boardEngName);

            }
        }
    }
}