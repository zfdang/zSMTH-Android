package com.zfdang.zsmth_android.models;

/**
 * Created by zfdang on 2016-3-14.
 */

import java.io.Serializable;

/**
 * A dummy item representing a piece of content.
 */
public class Topic implements Serializable {

    // this is actually a category, used in guidance fragment to seperate hot topics
    public Topic(String category) {
        this.category = category;
        isCategory = true;
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


    public static final String TYPE_BOTTOM = "d"; // 置底所包含的标记字符
    private String id;
    private String title;

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

    public String getBoardID() {
        return boardID;
    }

    public void setBoardID(String boardID) {
        this.boardID = boardID;
    }

    public String getBoardEngName() {
        return boardEngName;
    }

    public String getBoardName() {
        if(boardChsName != null && boardChsName.length() > 0) {
            return "[" + boardEngName +"]" + boardChsName;
        } else {
            return boardEngName;
        }
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

    private String author;
    private String boardID;
    private String boardEngName;
    private String boardChsName;

    public String getTopicID() {
        return topicID;
    }

    public void setTopicID(String topicID) {
        this.topicID = topicID;
    }

    private String topicID;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    private String category;
    //private Date date;
    private String dateString;
    private String type;

    private int totalPageNo;
    private int currentPageNo;
    public boolean isCategory;



    @Override
    public String toString() {
        if(isCategory) {
            return "Category " + this.category;
        } else {
            return String.format("(%s) %s by %s @ [%s]%s", this.topicID, this.title, this.author, this.boardEngName, this.boardChsName);
        }
    }
}