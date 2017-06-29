package com.zfdang.zsmth_android.newsmth;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.Html;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * this class will be used by Retrofit's API
 * Created by zfdang on 2016-4-4.
 */

//        "id": "mozilla",
//        "user_name": "Delivery Done!",
//        "face_url": "http://images.newsmth.net/nForum/uploadFace/M/mozilla.7812.jpg",
//        "face_width": 120,
//        "face_height": 120,
//        "gender": "m",
//        "astro": "白羊座",
//        "life": "楠木",
//        "lifelevel": 12,
//        "qq": "",
//        "msn": "",
//        "home_page": "",
//        "level": "用户",
//        "is_online": true,
//        "post_count": 7779,
//        "last_login_time": 1459772464,
//        "last_login_ip": "117.136.101.0",
//        "is_hide": true,
//        "is_activated": true,
//        "is_register": true,
//        "login_count": 21294,
//        "is_admin": false,
//        "first_login_time": 1066982906,
//        "stay_count": 101534530,
//        "score_user": 70002,
//        "score_manager": 0,
//        "plans": "aSM&nbsp;for&nbsp;Android:&nbsp;&nbsp;&nbsp;http://asm.zfdang.com/ <br /> zSMTH&nbsp;for&nbsp;iOS&nbsp;&nbsp;:&nbsp;&nbsp;&nbsp;http://zsmth.zfdang.com <br /> ",
//        "status": "目前在站上，状态如下：\n<span class='blue'>Web浏览</span> ",
//        "ajax_st": 1,
//        "ajax_code": "0005",
//        "ajax_msg": "操作成功"
//

public class UserInfo implements Parcelable {
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

  public int getAjax_st() {
    return ajax_st;
  }

  public void setAjax_st(int ajax_st) {
    this.ajax_st = ajax_st;
  }

  public String getAstro() {
    return astro;
  }

  public void setAstro(String astro) {
    this.astro = astro;
  }

  public static Creator<UserInfo> getCREATOR() {
    return CREATOR;
  }

  public int getFace_height() {
    return face_height;
  }

  public void setFace_height(int face_height) {
    this.face_height = face_height;
  }

  public String getFace_url() {
    return SMTHHelper.preprocessSMTHImageURL(face_url);
  }

  public void setFace_url(String face_url) {
    this.face_url = face_url;
  }

  public int getFace_width() {
    return face_width;
  }

  public void setFace_width(int face_width) {
    this.face_width = face_width;
  }

  public String getGender() {
    if ("f".equals(this.gender)) {
      return "女生";
    } else if ("m".equals(this.gender)) {
      return "男生";
    } else {
      return "未知";
    }
  }

  public void setGender(String gender) {
    this.gender = gender;
  }

  public String getHome_page() {
    return home_page;
  }

  public void setHome_page(String home_page) {
    this.home_page = home_page;
  }

  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }

  public boolean is_activated() {
    return is_activated;
  }

  public void setIs_activated(boolean is_activated) {
    this.is_activated = is_activated;
  }

  public boolean is_hide() {
    return is_hide;
  }

  public void setIs_hide(boolean is_hide) {
    this.is_hide = is_hide;
  }

  public boolean is_online() {
    return is_online;
  }

  public void setIs_online(boolean is_online) {
    this.is_online = is_online;
  }

  public boolean is_register() {
    return is_register;
  }

  public void setIs_register(boolean is_register) {
    this.is_register = is_register;
  }

  public String getLast_login_ip() {
    return last_login_ip;
  }

  public void setLast_login_ip(String last_login_ip) {
    this.last_login_ip = last_login_ip;
  }

  public String formatUnixTime(long value) {
    Date date = new Date(value * 1000L); // *1000 is to convert seconds to milliseconds
    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); // the format of your date
    String formattedDate = sdf.format(date);
    return formattedDate;
  }

  public String getLast_login_time() {
    return formatUnixTime(this.last_login_time);
  }

  public void setLast_login_time(int last_login_time) {
    this.last_login_time = last_login_time;
  }

  public String getLevel() {
    return level;
  }

  public void setLevel(String level) {
    this.level = level;
  }

  public String getLife() {
    return life;
  }

  public void setLife(String life) {
    this.life = life;
  }

  public int getLifelevel() {
    return lifelevel;
  }

  public void setLifelevel(int lifelevel) {
    this.lifelevel = lifelevel;
  }

  public String getLifeDesc() {
    return String.format("%s(%s)", this.life, this.lifelevel);
  }

  public String getLogin_count() {
    return String.format("%d", this.login_count);
  }

  public void setLogin_count(int login_count) {
    this.login_count = login_count;
  }

  public String getMsn() {
    return msn;
  }

  public void setMsn(String msn) {
    this.msn = msn;
  }

  public boolean isPlans() {
    return plans;
  }

  public void setPlans(boolean plans) {
    this.plans = plans;
  }

  public String getPost_count() {
    return String.format("%d", this.post_count);
  }

  public void setPost_count(int post_count) {
    this.post_count = post_count;
  }

  public String getQq() {
    return qq;
  }

  public void setQq(String qq) {
    this.qq = qq;
  }

  public String getScore_user() {
    return String.format("%d", this.score_user);
  }

  public void setScore_user(int score_user) {
    this.score_user = score_user;
  }

  public String getStatus() {
    // status : "目前在站上，状态如下：↵<span class='blue'>Web浏览</span> "
    return Html.fromHtml(status).toString();
  }

  public void setStatus(String status) {
    this.status = status;
  }

  public String getUser_name() {
    return user_name;
  }

  public void setUser_name(String user_name) {
    this.user_name = user_name;
  }

  String id;// yti,
  String user_name;// 为了她要快乐,
  String face_url;// http;////images.newsmth.net/nForum/img/face_default_m.jpg,
  int face_width;// 0,
  int face_height;// 0,
  String gender;// m,
  String astro;// 金牛座,
  String life;// 梧桐,
  int lifelevel;// 9,
  String qq;// ,
  String msn;// ,
  String home_page;// ,
  String level;// 用户,
  boolean is_online;// false,
  int post_count;// 896,

  public String getFirst_login_time() {
    if (this.first_login_time == 0) {
      return "";
    }
    return formatUnixTime(this.first_login_time);
  }

  public void setFirst_login_time(int first_login_time) {
    this.first_login_time = first_login_time;
  }

  int first_login_time; //
  int last_login_time;// 1459708507,
  String last_login_ip;// 60.190.231.*,
  boolean is_hide;// false,
  boolean is_activated;// true,
  boolean is_register;// true,
  int login_count;// 11386,
  int score_user;// 38376,
  boolean plans;// false,
  String status;// 目前不在站上,
  int ajax_st;// 1,
  String ajax_code;// 0005,
  String ajax_msg;// 操作成功

  @Override public String toString() {
    return "UserInfo{"
        + "ajax_code='"
        + ajax_code
        + '\''
        + ", id='"
        + id
        + '\''
        + ", user_name='"
        + user_name
        + '\''
        + ", face_url='"
        + face_url
        + '\''
        + ", face_width="
        + face_width
        + ", face_height="
        + face_height
        + ", gender='"
        + gender
        + '\''
        + ", astro='"
        + astro
        + '\''
        + ", life='"
        + life
        + '\''
        + ", lifelevel="
        + lifelevel
        + ", qq='"
        + qq
        + '\''
        + ", msn='"
        + msn
        + '\''
        + ", home_page='"
        + home_page
        + '\''
        + ", level='"
        + level
        + '\''
        + ", is_online="
        + is_online
        + ", post_count="
        + post_count
        + ", first_login_time="
        + first_login_time
        + ", last_login_time="
        + last_login_time
        + ", last_login_ip='"
        + last_login_ip
        + '\''
        + ", is_hide="
        + is_hide
        + ", is_activated="
        + is_activated
        + ", is_register="
        + is_register
        + ", login_count="
        + login_count
        + ", score_user="
        + score_user
        + ", plans="
        + plans
        + ", status='"
        + status
        + '\''
        + ", ajax_st="
        + ajax_st
        + ", ajax_msg='"
        + ajax_msg
        + '\''
        + '}';
  }

  @Override public int describeContents() {
    return 0;
  }

  @Override public void writeToParcel(Parcel dest, int flags) {
    dest.writeString(this.id);
    dest.writeString(this.user_name);
    dest.writeString(this.face_url);
    dest.writeInt(this.face_width);
    dest.writeInt(this.face_height);
    dest.writeString(this.gender);
    dest.writeString(this.astro);
    dest.writeString(this.life);
    dest.writeInt(this.lifelevel);
    dest.writeString(this.qq);
    dest.writeString(this.msn);
    dest.writeString(this.home_page);
    dest.writeString(this.level);
    dest.writeByte(is_online ? (byte) 1 : (byte) 0);
    dest.writeInt(this.post_count);
    dest.writeInt(this.first_login_time);
    dest.writeInt(this.last_login_time);
    dest.writeString(this.last_login_ip);
    dest.writeByte(is_hide ? (byte) 1 : (byte) 0);
    dest.writeByte(is_activated ? (byte) 1 : (byte) 0);
    dest.writeByte(is_register ? (byte) 1 : (byte) 0);
    dest.writeInt(this.login_count);
    dest.writeInt(this.score_user);
    dest.writeByte(plans ? (byte) 1 : (byte) 0);
    dest.writeString(this.status);
    dest.writeInt(this.ajax_st);
    dest.writeString(this.ajax_code);
    dest.writeString(this.ajax_msg);
  }

  public UserInfo() {
  }

  protected UserInfo(Parcel in) {
    this.id = in.readString();
    this.user_name = in.readString();
    this.face_url = in.readString();
    this.face_width = in.readInt();
    this.face_height = in.readInt();
    this.gender = in.readString();
    this.astro = in.readString();
    this.life = in.readString();
    this.lifelevel = in.readInt();
    this.qq = in.readString();
    this.msn = in.readString();
    this.home_page = in.readString();
    this.level = in.readString();
    this.is_online = in.readByte() != 0;
    this.post_count = in.readInt();
    this.first_login_time = in.readInt();
    this.last_login_time = in.readInt();
    this.last_login_ip = in.readString();
    this.is_hide = in.readByte() != 0;
    this.is_activated = in.readByte() != 0;
    this.is_register = in.readByte() != 0;
    this.login_count = in.readInt();
    this.score_user = in.readInt();
    this.plans = in.readByte() != 0;
    this.status = in.readString();
    this.ajax_st = in.readInt();
    this.ajax_code = in.readString();
    this.ajax_msg = in.readString();
  }

  public static final Creator<UserInfo> CREATOR = new Creator<UserInfo>() {
    @Override public UserInfo createFromParcel(Parcel source) {
      return new UserInfo(source);
    }

    @Override public UserInfo[] newArray(int size) {
      return new UserInfo[size];
    }
  };
}
