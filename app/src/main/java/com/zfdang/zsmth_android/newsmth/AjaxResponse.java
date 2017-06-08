package com.zfdang.zsmth_android.newsmth;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by zfdang on 2016-4-5.
 */

//{"ajax_code":"0406","list":[{"text":"版面:测试专用版面(Test)","url":"/board/Test"},
// {"text":"主题:测试王安国","url":"/article/Test/910626"},
// {"text":"水木社区","url":"/mainpage"}],
// "default":"/board/Test","ajax_st":1,"ajax_msg":"发表成功"}
//
//{"ajax_st":0,"ajax_code":"0204","ajax_msg":"您无权在本版发表文章"}

//{
//        "num": "8",
//        "type": "inbox",
//        "content": "转寄人: zSMTHDev (zSMTHDev) <br /> 标&nbsp;&nbsp;题: Re: 手动挡起步难开的原因 <br /> 发信站: 水木社区 (Sat Apr 30 17:36:58 2016) <br /> 来&nbsp;&nbsp;源: 114.240.83.208 <br />&nbsp;&nbsp;<br /> <font class=\"f011\">【以下内容由 </font><font class=\"f010\">zSMTHDev</font><font class=\"f011\"> 转寄于 </font><font class=\"f010\">AutoWorld</font><font class=\"f011\"> 版】</font><font class=\"f000\"> <br /> 发信人: lef (人道三十), 信区: AutoWorld <br /> 标&nbsp;&nbsp;题: Re: 手动挡起步难开的原因 <br /> 发信站: 水木社区 (Sat Apr 30 00:18:01 2016), 站内 <br />&nbsp;&nbsp;<br />&nbsp;&nbsp;仔细看了看，发现你手动挡南开的原因是技术太差 <br /> 【 在 s200602152 (holic) 的大作中提到: 】 <br /> </font><font class=\"f006\">: 我觉得是抬离合车加速的设定。 </font> <br /> <font class=\"f006\">: 抬离合后车往前走，脚根据惯性相对车会往后移，加速了抬离合的过程。 </font> <br /> <font class=\"f006\">: 车往前走的加速度越大，脚根据惯性往后移（变相地又抬了离合）的幅度越大，就越容易熄火。 </font> <br /> <font class=\"f006\">: 但是抬的太慢又会岂不很慢。 </font> <br /> <font class=\"f006\">: 让人不好掌控。 </font> <br /> <font class=\"f006\">: 还是应该设定成抬离合电脑自动给油才好，无论抬得多快都不会熄火。 </font> <br /> <font class=\"f006\">: 那么问题来了，有这样的车吗？ </font> <br />&nbsp;&nbsp;<br />&nbsp;&nbsp;<br /> -- <br />&nbsp;&nbsp;<br /> <font class=\"f000\"></font><font class=\"f015\">※ 来源:·水木社区 newsmth.net·[FROM: 58.60.168.146]</font><font class=\"f000\"> <br /> </font>",
//        "ajax_st": 1,
//        "ajax_code": "0005",
//        "ajax_msg": "操作成功"
//}

public class AjaxResponse implements Parcelable {
  public static final int AJAX_RESULT_OK = 1;
  public static final int AJAX_RESULT_FAILED = 0;
  public static final int AJAX_RESULT_UNKNOWN = 2;

  private int ajax_st;
  private String ajax_code;
  private String ajax_msg;
  private String content;
  private int group_id;

  public AjaxResponse() {
  }

  public int getAjax_st() {
    return ajax_st;
  }

  public void setAjax_st(int ajax_st) {
    this.ajax_st = ajax_st;
  }

  public String getAjax_code() {
    return ajax_code;
  }

  public void setAjax_code(String ajax_code) {
    this.ajax_code = ajax_code;
  }

  public String getAjax_msg() {
    return ajax_msg;
  }

  public void setAjax_msg(String ajax_msg) {
    this.ajax_msg = ajax_msg;
  }

  public String getContent() {
    return content;
  }

  public void setContent(String content) {
    this.content = content;
  }

  public int getGroup_id() {
    return group_id;
  }

  public void setGroup_id(int group_id) {
    this.group_id = group_id;
  }

  @Override public String toString() {
    return "AjaxResponse{"
        + "ajax_code='"
        + ajax_code
        + '\''
        + ", ajax_st="
        + ajax_st
        + ", ajax_msg='"
        + ajax_msg
        + '\''
        + ", content='"
        + content
        + '\''
        + ", group_id="
        + group_id
        + '}';
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(this.ajax_st);
    dest.writeString(this.ajax_code);
    dest.writeString(this.ajax_msg);
    dest.writeString(this.content);
    dest.writeInt(this.group_id);
  }

  protected AjaxResponse(Parcel in) {
    this.ajax_st = in.readInt();
    this.ajax_code = in.readString();
    this.ajax_msg = in.readString();
    this.content = in.readString();
    this.group_id = in.readInt();
  }

  public static final Creator<AjaxResponse> CREATOR = new Creator<AjaxResponse>() {
    @Override public AjaxResponse createFromParcel(Parcel source) {
      return new AjaxResponse(source);
    }

    @Override public AjaxResponse[] newArray(int size) {
      return new AjaxResponse[size];
    }
  };
}
