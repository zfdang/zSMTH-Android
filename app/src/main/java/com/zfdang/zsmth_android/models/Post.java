package com.zfdang.zsmth_android.models;

import android.text.Html;
import android.text.Spanned;
import android.util.Log;

import com.zfdang.zsmth_android.helpers.StringUtils;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * Post object.
 * Created by zfdang on 2016-3-14.
 */
public class Post {
    public static int ACTION_DEFAULT = 0;
    public static int ACTION_FIRST_POST_IN_SUBJECT = 1;
    public static int ACTION_PREVIOUS_POST_IN_SUBJECT = 2;
    public static int ACTION_NEXT_POST_IN_SUBJECT = 3;
    private String postID;
    private String title;
    private String author;
    private String nickName;
    private Date date;
    private String htmlContent;
    private List<String> likes;
    private List<Attachment> attachFiles;

    public Post() {
        date = new Date();
    }

    public void setLikes(List<String> likes) {
        this.likes = likes;
    }

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
        final int MAX_NICKNAME_LENGTH = 12;
        if (nickName.length() > MAX_NICKNAME_LENGTH) {
            nickName = nickName.substring(0, MAX_NICKNAME_LENGTH) + "..";
        }
        this.nickName = nickName;
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
        if (nickName == null || nickName.length() == 0) {
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

    /*
    the expected input is formatted plain text, no html tag is expected
    line break by \n, but not <br>
    */
    private String processPostContent(String content) {
        // &nbsp; is converted as code=160, but not a whitespace (ascii=32)
        // http://stackoverflow.com/questions/4728625/why-trim-is-not-working
        content = content.replace(String.valueOf((char) 160), " ");

        String[] lines = content.split("\n");

        // find signature start line
        int signatureStartLine = -1;
        for (int i = lines.length - 1; i >= 0; i--) {
            String line = lines[i];
            if (line.startsWith("--")) {
                // find the first "--" from the last to the first
                signatureStartLine = i;
                break;
            }
        }

        // process content line by line
        StringBuilder sb = new StringBuilder();
        int linebreak = 0;
        int signatureMode = 0;
        for (int i = 0; i < lines.length; i++) {
            String line = lines[i];

            if ((line.startsWith("发信人:") || line.startsWith("寄信人:")) && i <= 3) {
                // find nickname for author here, skip the line
                // 发信人: schower (schower), 信区: WorkLife
                String nickName = StringUtils.subStringBetween(line, "(", ")");
                if (nickName != null && nickName.length() > 0) {
                    this.setNickName(nickName);
                }
                continue;
            } else if (line.startsWith("标  题:") && i <= 3) {
                // skip this line
                continue;
            } else if (line.startsWith("发信站:") && i <= 3) {
                // find post date here, skip the line
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

            // handle quoted content
            if (line.startsWith(":")) {
                line = "<font color=#00b4ae>" + line + "</font>";
                sb.append(line).append("<br />");
                continue;
            }

            if (line.trim().length() == 0) {
                linebreak++;
                if (linebreak >= 2) {
                    // continuous linebreak, skip extra linebreak
                    continue;
                } else {
                    sb.append(line).append("<br />");
                    continue;
                }
            } else {
                // reset counter
                linebreak = 0;
            }

            // handle siguature
            // we have to make sure "--" is the last one, it might appear in post content body
            if (i == signatureStartLine) {
                // entering signature mode
                signatureMode = 1;
                sb.append(line).append("<br />");
                continue;
            }

            // ※ 修改:·wpd419 于 Mar 29 09:43:17 2016 修改本文·[FROM: 111.203.75.*]
            // ※ 来源:·水木社区 http://www.newsmth.net·[FROM: 111.203.75.*]
            if (line.contains("※ 来源:·")) {
                // jump out of signature mode
                signatureMode = 0;
                line = line.replace("·", "").replace("http://www.newsmth.net", "").replace("http://m.newsmth.net", "").replace("newsmth.net", "");
                line = "<font color=#727272>" + StringUtils.lookupIPLocation(line) + "</font>";
                sb.append(line).append("<br />");
                continue;
            } else if (line.contains("※ 修改:·")) {
                // jump out of signature mode
                signatureMode = 0;
                line = line.replace("·", "").replace("修改本文", "");
                line = "<font color=#727272>" + StringUtils.lookupIPLocation(line) + "</font>";
                sb.append(line).append("<br />");
                continue;
            }

            // after handle last part of post content, if it's still in signature mode, add signature
            if (signatureMode == 1) {
                line = "<font color=#727272>" + line + "</font>";
                sb.append(line).append("<br />");
                continue;
            }

            // for other normal line, add it directly
            sb.append(line).append("<br />");
        }

        return sb.toString().trim();
    }

    public void setContentFromElement(Element content) {
        Log.d("ContentFromElement", content.html());

        // find all attachment from node
        // <a target="_blank" href="http://att.newsmth.net/nForum/att/AutoWorld/1939790539/4070982">
        // <img border="0" title="单击此查看原图" src="http://att.newsmth.net/nForum/att/AutoWorld/1939790539/4070982/large" class="resizeable">
        // </a>
        Elements as = content.select("a[href]");
        for (Element a : as) {
            Elements imgs = a.select("img[src]");
            if (imgs.size() == 1) {
                // find one image attachment
                String imgsrc = imgs.attr("src");
                String imgsrc_orig = a.attr("href");
                Attachment attach = new Attachment(imgsrc_orig);

                this.addAttachFile(attach);

                // replace a[href] with MARK
                a.html("###ZSMTH_ATTACHMENT###");
            }
        }

        // set content
        String formattedPlainText = Html.fromHtml(content.html()).toString();
        this.htmlContent = this.processPostContent(formattedPlainText);
    }

    public String getRawContent() {
        return Html.fromHtml(this.htmlContent).toString();
    }

    public Spanned getSpannedContent() {
        String finalContent = this.htmlContent;

        if (likes != null && likes.size() > 0) {
            StringBuilder wordList = new StringBuilder();
            wordList.append("<br/>");
            for (String word : likes) {
                wordList.append(word).append("<br/>");
            }
            finalContent += new String(wordList);
        }

        return Html.fromHtml(finalContent);
    }

    public void addAttachFile(Attachment attach) {
        if (attachFiles == null) {
            attachFiles = new ArrayList<>();
        }
        if (attach != null) {
            attachFiles.add(attach);
        }
    }

    public int getNumberOfAttachFiles() {
        if(attachFiles != null) {
            return attachFiles.size();
        }
        return 0;
    }

    public Attachment getAttachFileByIndex(int index) {
        if(attachFiles != null && index < attachFiles.size()) {
            return attachFiles.get(index);
        }
        return null;
    }

}
