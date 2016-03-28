package com.zfdang.zsmth_android.models;
import android.text.Html;
import android.text.Spanned;

import com.zfdang.zsmth_android.helpers.StringUtils;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by zfdang on 2016-3-14.
 */
public class Post {
    private String postID;
    private String title;
    private String author;
    private String nickName;
    private Date date;
    private String content;

    private ArrayList<Attachment> attachFiles;

    @Override
    public String toString() {
        return "Post{" +
                ", postID='" + postID + '\'' +
                ", title='" + title + '\'' +
                ", date=" + date +
                ", author='" + author + '\'' +
                ", nickName='" + nickName + '\'' +
                '}';
    }

    public void setNickName(String nickName) {
        if(nickName.length() > 12) {
            nickName = nickName.substring(0, 12) + "...";
        }
        this.nickName = nickName;
    }


    public static int ACTION_DEFAULT = 0;
    public static int ACTION_FIRST_POST_IN_SUBJECT = 1;
    public static int ACTION_PREVIOUS_POST_IN_SUBJECT = 2;
    public static int ACTION_NEXT_POST_IN_SUBJECT = 3;

    public Post() {
        date = new Date();
    }

    public String getPostID() {
        return postID;
    }

    public void setPostID(String postID) {
        this.postID = postID;
    }

    public String getTitle() {
        return this.title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        if(nickName == null || nickName.length() == 0){
            return this.author;
        } else {
            return String.format("%s(%s)", this.author, this.nickName);
        }
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public void setDate(Date date) {
        this.date = date;
    }

    public String getFormatedDate() {
        return StringUtils.getFormattedString(this.date);
    }

    public void setContent(String content) {
        // content is expected to be HTML segment
        this.content = content;
    }

    public Spanned getSpannedContent() {
        Spanned result = Html.fromHtml(this.content);
        return result;
    }

    public ArrayList<Attachment> getAttachFiles() {
        return attachFiles;
    }
    public void setAttachFiles(ArrayList<Attachment> attachFiles) {
        this.attachFiles = attachFiles;
    }

}
