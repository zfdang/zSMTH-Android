package com.zfdang.zsmth_android.models;

import android.text.Html;
import android.text.TextUtils;
import android.util.Log;

import com.zfdang.zsmth_android.Settings;
import com.zfdang.zsmth_android.helpers.StringUtils;
import com.zfdang.zsmth_android.newsmth.SMTHHelper;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * Post object.
 * Created by zfdang on 2016-3-14.
 */
public class Post {
  private static final String ATTACHMENT_MARK = "###ZSMTH_ATTACHMENT###";
  public static int ACTION_DEFAULT = 0;
  public static int ACTION_FIRST_POST_IN_SUBJECT = 1;
  public static int ACTION_PREVIOUS_POST_IN_SUBJECT = 2;
  public static int ACTION_NEXT_POST_IN_SUBJECT = 3;
  private String postID;
  private String title;
  private String author;
  private String nickName;
  private Date date;
  private String position;

  private List<Like> likes;
  private List<Attachment> attachFiles;

  private String htmlContent; // likes are not included
  private String htmlContentAndLikes; // contents and likes are merged together
  private List<ContentSegment> mSegments;  // parsed from htmlCompleteContent

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
    if (nickName == null || nickName.length() == 0) {
      return this.author;
    } else {
      return String.format("%s(%s)", this.author, this.nickName);
    }
  }

  public void setAuthor(String author) {
    this.author = author;
  }

  public String getRawAuthor() {
    return this.author;
  }

  public void setNickName(String nickName) {
    final int MAX_NICKNAME_LENGTH = 12;
    if (nickName.length() > MAX_NICKNAME_LENGTH) {
      nickName = nickName.substring(0, MAX_NICKNAME_LENGTH) + "..";
    }
    this.nickName = nickName;
  }

  public void setDate(Date date) {
    this.date = date;
  }

  public String getFormatedDate() {
    return StringUtils.getFormattedString(this.date);
  }

  public String getPosition() {
    return position;
  }

  public void setPosition(String position) {
    this.position = position;
  }

  public void parsePostContent(Element content, boolean isPost) {
    Element pureContent = null;
    if (isPost) {
      // 1. parse likes node first
      // <div class="likes">
      Elements likeNodes = content.select("div.likes");
      if (likeNodes.size() == 1) {
        Element likeNode = likeNodes.first();
        ParseLikeElementInPostContent(likeNode);
        likeNode.remove();
      }

      // 2. remove like button
      // <button class="button add_like"
      Elements likeButtons = content.select("button.add_like");
      if(likeButtons.size() == 1) {
        Element likeButton = likeButtons.get(0);
        likeButton.remove();
      }

      // on Oct.19, SMTH add topic id in front of author
      // <font style="display: none">1026567117</font>发信人: areshuang (壹剑客), 信区: SecondDigi
      // remove this meaningless block
      for (Element font : content.select("font[style='display: none']")) {
        font.remove();
      }

      // 4. take td content as the pure post content
      pureContent = Jsoup.parse(content.html());
    } else {
      // from email, content not enclosed by <p> tag
      pureContent = content;
    }

    // 2. parse post pure content, result in htmlContent
    parsePostPureContent(pureContent);

    // 3. merge contents and likes, result in htmlContentAndLikes
    mergePureContentAndLikes();

    // 4. parse contents into segments
    parseContentToSegments();
  }

    // parse like list in post content
    public void ParseLikeElementInPostContent(Element likeNode) {
      if(likes == null) {
          likes = new ArrayList<>();
      }
      likes.clear();

        // <div class="like_name">有36位用户评价了这篇文章：</div>
        Elements nodes = likeNode.select("div.like_name");
        if (nodes.size() == 1) {
            Element node = nodes.first();
        }

        // <li><span class="like_score_0">[&nbsp;&nbsp;]</span><span class="like_user">fly891198061:</span>
        // <span class="like_msg">无法忍受，我不会变节，先斗智，不行就自杀！来个痛快的~！</span>
        // <span class="like_time">(2016-03-27 15:04)</span></li>
        nodes = likeNode.select("li");
        for (Element n : nodes) {
          Elements spans = n.select("span");
          if(spans.size() == 4) {
            Like like = new Like(spans.get(0).text(), spans.get(1).text(), spans.get(2).text(), spans.get(3).text());
            likes.add(like);
          }
        }
    }

  // parse post pure content, then merge them to htmlCompleteContent, then split it into htmlSegments
  public void parsePostPureContent(Element content) {
    // find all attachment from node
    // <a target="_blank" href="http://att.newsmth.net/nForum/att/AutoWorld/1939790539/4070982">
    // <img border="0" title="单击此查看原图" src="http://att.newsmth.net/nForum/att/AutoWorld/1939790539/4070982/large" class="resizeable">
    // </a>
    // special image attachment: webp. newsmth does not support webp now, so it's not displayed as img
    // <a href="http://att.newsmth.net/nForum/att/MMJoke/1634780953/211" target="_blank">1.webp</a>

    // new image URL after 2017-06-28
    // <a target="_blank" href="//att.newsmth.net/nForum/att/Picture/1726713/228774">
    // <img border="0" title="单击此查看原图" src="//att.newsmth.net/nForum/att/Picture/1726713/228774/large" class="resizeable" /></a>

    // new image url after 2020-06-10
    // <a target="_blank" href="//static.mysmth.net/nForum/att/FamilyLife/1763462541/17096">
    // <img border="0" title="单击此查看原图" src="//static.mysmth.net/nForum/att/FamilyLife/1763462541/17096/large" class="resizeable" /></a>

    // other attachment
    // 		<a href="//static.mysmth.net/nForum/att/Test/943486/245" target="_blank">《三国演义》_(果麦经典)_罗贯中.epub</a>

    Elements as = content.select("a[href]");
    for (Element a : as) {
      // process each a|href
      Elements imgs = a.select("img[src]");
      if (imgs.size() == 1) {
        // find one image attachment
        String origImageSrc = a.attr("href");

        Element img = imgs.first();
        String resizedImageSrc = img.attr("src");

        Attachment attach = new Attachment(origImageSrc, resizedImageSrc);
        this.addAttachFile(attach);

        // replace a[href] with MARK
        // we will split the string with MARK, so make sure no two MAKR will stay together
        a.html(ATTACHMENT_MARK + " ");
      } else if (imgs.size() == 0) {
        // does not find any image element, handle the special webp
        String attachName = a.text();
        if (attachName != null && attachName.endsWith(".webp")) {
          // this is a webp attachment, show it as image
          String origImageSrc = a.attr("href");
          Attachment attach = new Attachment(origImageSrc, origImageSrc);
          this.addAttachFile(attach);

          a.html(ATTACHMENT_MARK + " ");
        } else {
          // other attachment, add link for downloading
          String downloadURL = a.attr("href");
          downloadURL = SMTHHelper.preprocessSMTHImageURL(downloadURL);
          if (downloadURL.contains("/nForum/att/")){
            a.append("<br>" + downloadURL);
          }
        }
      }
    }

    // process pure post content
    // it's important to know that not all HTML tags are supported by Html.fromHtml, see the supported list
    // https://commonsware.com/blog/Android/2010/05/26/html-tags-supported-by-textview.html
    // http://stackoverflow.com/questions/18295881/android-textview-html-font-size-tag
    String formattedEscapedPlainText = TextUtils.htmlEncode(Html.fromHtml(content.html()).toString());
    this.htmlContent = this.parsePostPureContentFormat(formattedEscapedPlainText);
  }

  public void mergePureContentAndLikes(){
      htmlContentAndLikes = this.htmlContent;

      if (likes != null && likes.size() > 0) {
        StringBuilder wordList = new StringBuilder();
        wordList.append("有" + likes.size() + "位用户评价了这篇文章:");
        wordList.append("<br/>");
        wordList.append("<small>");
        for (Like like : likes) {
          wordList.append("[<font face='monospace'>" + like.score + "</font>]");
          wordList.append(" " + like.msg);
          wordList.append(" ( " + like.user);
          wordList.append(" @ " + like.time);
          wordList.append(" )<br/>");
        }
        wordList.append("</small>");
        htmlContentAndLikes += new String(wordList);
    }
  }

  // split complete content with ATTACHMENT_MARK
  private void parseContentToSegments() {
    if (mSegments == null) {
      mSegments = new ArrayList<>();
    }
    mSegments.clear();

    if (attachFiles == null || attachFiles.size() == 0) {
      // no attachment, add all content as one segment
      mSegments.add(new ContentSegment(ContentSegment.SEGMENT_TEXT, htmlContentAndLikes));
    } else {
      // when there are attachments here, separate them one by one
      String[] segments = htmlContentAndLikes.split(ATTACHMENT_MARK);

      // add segments and attachments together
      int attachIndex = 0;
      for (String segment : segments) {
        //                Log.d("Splited Result:", String.format("{%s}", segment));
        // add segment to results if it's not empty,
        // MARK are seperated by several <br />, we should skip these seperated text
        if (!StringUtils.isEmptyString(segment) || attachIndex == 0) {
          // since we expect there will always be a textview before imageview
          // even the first text segment is empty, we still add it
          mSegments.add(new ContentSegment(ContentSegment.SEGMENT_TEXT, segment));
        }

        // add next image attachment to results
        if (attachFiles != null && attachIndex < attachFiles.size()) {
          Attachment attach = attachFiles.get(attachIndex);
          String imageURL = null;
          if (Settings.getInstance().isLoadOriginalImage()) {
            imageURL = attach.getOriginalImageSource();
          } else {
            imageURL = attach.getResizedImageSource();
          }
          ContentSegment img = new ContentSegment(ContentSegment.SEGMENT_IMAGE, imageURL);
          img.setImgIndex(attachIndex);
          mSegments.add(img);
        }

        attachIndex++;
      }
    }

    //        Log.d("ContentSegment", String.format("Total segments here: %d", mSegments.size()));
    //        for (ContentSegment content : mSegments) {
    //            if (content.getType() == ContentSegment.SEGMENT_IMAGE) {
    //                Log.d("ContentSegment", String.format("Image %s, index = %d", content.getUrl(), content.getImgIndex()));
    //            } else if (content.getType() == ContentSegment.SEGMENT_TEXT) {
    //                Log.d("ContentSegment", String.format("Text, {%s}", content.getSpanned().toString()));
    //            }
    //        }
  }

  /*
  the expected input is formatted plain text, no html tag is expected
  line break by \n, but not <br>
  */
  private String parsePostPureContentFormat(String content) {
    // &nbsp; is converted as code=160, but not a whitespace (ascii=32)
    // http://stackoverflow.com/questions/4728625/why-trim-is-not-working
    content = content.replace(String.valueOf((char) 160), " ");

    // it's important to know that not all HTML tags are supported by Html.fromHtml, see the supported list
    // https://commonsware.com/blog/Android/2010/05/26/html-tags-supported-by-textview.html
    String[] lines = content.split("\n");

    // find signature start line
    int signatureStartLine = -1;
    for (int i = lines.length - 1; i >= 0; i--) {
      String line = lines[i];
      if (line.startsWith("--") && line.length() <= 3) {
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

      // handle ATTACH_MARK
      if (line.contains(ATTACHMENT_MARK)) {
        sb.append(line);
        continue;
      }

      // handle quoted content
      if (line.startsWith(":")) {
        line = "<font color=#299999>" + line + "</font>";
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

      // handle signature
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
        line = line.replace("·", "")
            .replace("http://www.mysmth.net", "")
            .replace("http://www.newsmth.net", "")
            .replace("http://m.mysmth.net", "")
            .replace("http://m.newsmth.net", "")
            .replace("http://mysmth.net", "")
            .replace("https://exp.mysmth.net", "")
            .replace("http://newsmth.net", "")
            .replace("mysmth.net", "")
            .replace("newsmth.net", "")
            .replace("m.mysmth.net", "")
            .replace("m.newsmth.net", "")
            .replace("官方应用", "")
            .replace("客户端", "");

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
        line = "<small><font color=#727272>" + line + "</font></small>";
        sb.append(line).append("<br />");
        continue;
      }

      // for other normal line, add it directly
      sb.append(line).append("<br />");
    }

    return sb.toString().trim();
  }

  public List<ContentSegment> getContentSegments() {
    return mSegments;
  }

  // used by copy post content menu, or quoted content while replying
  public String getRawContent() {
    return Html.fromHtml(this.htmlContent.replace(ATTACHMENT_MARK, "")).toString();
  }

  // this method will be called when post content can't be fetched properly
  // error message is returned in this case
  public void setRawContent(String rawContent) {
    this.htmlContent = rawContent;
    this.htmlContentAndLikes = rawContent;
    parseContentToSegments();
  }

  public void addAttachFile(Attachment attach) {
    if (attachFiles == null) {
      attachFiles = new ArrayList<>();
    }
    if (attach != null) {
      attachFiles.add(attach);
    }
  }

  public List<Attachment> getAttachFiles() {
    return attachFiles;
  }

  @Override public String toString() {
    return "Post{"
        + "postID='"
        + postID
        + '\''
        + ", title='"
        + title
        + '\''
        + ", author='"
        + author
        + '\''
        + ", nickName='"
        + nickName
        + '\''
        + ", date="
        + date
        + ", position='"
        + position
        + '\''
        + '}';
  }
}
