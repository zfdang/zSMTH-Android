package com.zfdang.zsmth_android.models;
import java.util.ArrayList;
import java.util.Date;

import android.text.Html;

/**
 * Created by zfdang on 2016-3-14.
 */
public class Post {
    private String subjectID;
    private String topicSubjectID;
    private String title;
    private String author;
    private String board;
    private String boardID;
    private Date date;
    private String index;
    private CharSequence content;
    private ArrayList<Attachment> attachFiles;

    public static int ACTION_DEFAULT = 0;
    public static int ACTION_FIRST_POST_IN_SUBJECT = 1;
    public static int ACTION_PREVIOUS_POST_IN_SUBJECT = 2;
    public static int ACTION_NEXT_POST_IN_SUBJECT = 3;

    public Post() {
        date = new Date();
    }

    @Override
    public String toString() {
        return this.content.toString();
    }

    public String getSubjectID() {
        return subjectID;
    }

    public void setSubjectID(String subjectid) {
        this.subjectID = subjectid;
    }

    public void setTopicSubjectID(String topicSubjectID) {
        this.topicSubjectID = topicSubjectID;
    }

    public String getTopicSubjectID() {
        return topicSubjectID;
    }

    public String getTitle() {
        return this.title;
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

    public String getPostIndex() {
        return index;
    }

    public void setPostIndex(String index) {
        this.index = index;
    }

    public String getBoard() {
        return board;
    }

    public void setBoard(String board) {
        this.board = board;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public Date getDate() {
        return date;
    }

    public void setContent(String content) {
        this.content = Html.fromHtml(content);
    }

    public String getTextContent() {
        String text = content.toString();
        return text;
    }

    public CharSequence getContent() {
        return content;
    }

    public void setBoardID(String boardid) {
        this.boardID = boardid;
    }

    public String getBoardID() {
        return boardID;
    }

    public ArrayList<Attachment> getAttachFiles() {
        return attachFiles;
    }

    public void setAttachFiles(ArrayList<Attachment> attachFiles) {
        this.attachFiles = attachFiles;
    }

}
