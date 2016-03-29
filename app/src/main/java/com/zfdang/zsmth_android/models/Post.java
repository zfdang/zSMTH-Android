package com.zfdang.zsmth_android.models;

import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import com.zfdang.zsmth_android.helpers.StringUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by zfdang on 2016-3-14.
 */
public class Post {
    private String postID;
    private String title;
    private String author;
    private String nickName;
    private Date date;
    private String htmlContent;


    private List<String> likes;
    public void setLikes(List<String> likes) {
        this.likes = likes;
    }

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


    private  String processPostContent(String content) {
        Log.d("processPostContent", content);
        content = content.replace("\\n", "\n").replace("\\r", "\r")
                .replace("\\/", "/").replace("\\\"", "\"").replace("\\'", "'");


        // process content line by line
        String[] lines = content.split("\n");
        StringBuilder sb = new StringBuilder();
        int linebreak = 0;
        int linequote = 0;
        int seperator = 0;
        for (String line : lines) {
            if (line.startsWith("发信人:") || line.startsWith("寄信人:")) {
                // find nickname for author here
                // 发信人: schower (schower), 信区: WorkLife
                String nickName = StringUtils.subStringBetween(line, "(", ")");
                if(nickName != null && nickName.length() > 0) {
                    this.setNickName(nickName);
                }
                continue;
            } else if (line.startsWith("标  题:")) {
                continue;
            } else if (line.startsWith("发信站:")) {
                // <br /> 发信站: 水木社区 (Fri Mar 25 11:52:04 2016), 站内
                line = StringUtils.subStringBetween(line, "(", ")");
                SimpleDateFormat simpleFormat = new SimpleDateFormat("EEE MMM d HH:mm:ss yyyy", Locale.US);
                try {
                    Date localdate = simpleFormat.parse(line);
                    this.setDate(localdate);
                    continue;
                } catch (ParseException e1) {
                    e1.printStackTrace();
                }
            }
            if (line.equals("--")) {
                if (seperator > 1) {
                    break;
                }

                seperator++;
            } else {
                if (seperator > 0) {
                    if (line.length() > 0) {
                        line = "<font color=#33CC66>" + line + "</font>";
                    } else {
                        continue;
                    }
                }
            }

            if (line.startsWith(":")) {
                linequote++;
                if (linequote > 5) {
                    continue;
                } else {
                    line = "<font color=#006699>" + line + "</font>";
                }
            } else {
                linequote = 0;
            }

            if (line.equals("")) {
                linebreak++;
                if (linebreak > 1) {
                    continue;
                }
            } else {
                linebreak = 0;
            }

            // ※ 修改:·wpd419 于 Mar 29 09:43:17 2016 修改本文·[FROM: 111.203.75.*]
            // ※ 来源:·水木社区 http://www.newsmth.net·[FROM: 111.203.75.*]
            if (line.contains("※ 来源:·") || line.contains("※ 修改:·")) {
                // remove ASCII control first
                Pattern cPattern = Pattern.compile("※[^\\]]*\\]");
                Matcher cMatcher = cPattern.matcher(line);
                if(cMatcher.find()){
                    line = cMatcher.group(0);
                }

//                if (aSMApplication.getCurrentApplication().isShowIp()) {
//                    Pattern myipPattern = Pattern.compile("FROM[: ]*(\\d{1,3}\\.\\d{1,3}\\.\\d{1,3}\\.)[\\d\\*]+");
//                    Matcher myipMatcher = myipPattern.matcher(line);
//                    while (myipMatcher.find()) {
//                        String ipl = myipMatcher.group(1);
//                        if (ipl.length() > 5) {
//                            ipl = "<font color=\"#c0c0c0\">FROM $1\\*("
//                                    + aSMApplication.db.getLocation(ipl + "1") + ")<\\/font>";
//                        } else {
//                            ipl = "<font color=\"#c0c0c0\">FROM $1\\*<\\/font>";
//                        }
//                        line = myipMatcher.replaceAll(ipl);
//                    }
//                }
            }
            sb.append(line).append("<br />");
        }

        String result = sb.toString().trim();
        return result;
    }

    public void setContent(String content) {
        // content is expected to be HTML segment
        // element.html()
        String temp = Html.fromHtml(content).toString();
        this.htmlContent = this.processPostContent(temp);;
    }

    public Spanned getSpannedContent() {
        String finalContent = this.htmlContent;
        if(likes != null && likes.size() > 0) {
            StringBuilder wordList = new StringBuilder();
            for (String word : likes) {
                wordList.append("<br/>" + word);
            }

            finalContent += new String(wordList);
        }

        Spanned result = Html.fromHtml(finalContent);
        return result;
    }

    public ArrayList<Attachment> getAttachFiles() {
        return attachFiles;
    }
    public void setAttachFiles(ArrayList<Attachment> attachFiles) {
        this.attachFiles = attachFiles;
    }

}
