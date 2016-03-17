package com.zfdang.zsmth_android.models;

/**
 * Created by zfdang on 2016-3-14.
 */

import java.io.Serializable;

/**
 * A dummy item representing a piece of content.
 */
public class Topic implements Serializable {
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
        return "[" + boardEngName +"]" + boardChsName;
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
    //private Date date;
    private String dateString;
    private String type;

    private int totalPageNo;
    private int currentPageNo;

    public Topic() {
    }


    @Override
    public String toString() {
        return getBoardName();
    }
}