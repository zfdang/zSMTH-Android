package com.zfdang.zsmth_android.newsmth;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * used by Observable<UserStatus> queryActiveUserStatus()
 * Created by zfdang on 2016-4-5.
 */
//{
//        "id": "guest",
//        "user_name": "和谐水木",
//        "face_url": "http://images.newsmth.net/nForum/img/face_default_f.jpg",
//        "face_width": null,
//        "face_height": null,
//        "gender": "f",
//        "astro": "未知",
//        "life": "椽桷",
//        "lifelevel": 13,
//        "qq": null,
//        "msn": null,
//        "home_page": null,
//        "level": "新人",
//        "is_online": true,
//        "post_count": 74,
//        "last_login_time": 1459896513,
//        "last_login_ip": "218.17.158.134",
//        "is_hide": true,
//        "is_activated": true,
//        "is_register": false,
//        "login_count": 5,
//        "is_admin": false,
//        "first_login_time": 917006225,
//        "stay_count": 5854476597,
//        "score_user": 2016,
//        "score_manager": 0,
//        "is_login": false,
//        "forum_totol_count": 22549,
//        "forum_user_count": 6115,
//        "forum_guest_count": 16434,
//        "new_mail": true,
//        "full_mail": false,
//        "new_like": 0,
//        "new_reply": false,
//        "new_at": false,
//        "new_msg": -1,
//        "ajax_st": 1,
//        "ajax_code": "0005",
//        "ajax_msg": "操作成功"
//}


public class UserStatus implements Parcelable {

    private String id;
    private String face_url;
    private boolean is_login;

    private boolean new_mail;
    private boolean full_mail;

    // // sometime, this is a boolean, sometime, it is a integer. SMTH is inconsistent
    private int new_like;

    public int getNew_reply() {
        return new_reply;
    }

    public void setNew_reply(int new_reply) {
        this.new_reply = new_reply;
    }

    public int getNew_like() {
        return new_like;
    }

    public void setNew_like(int new_like) {
        this.new_like = new_like;
    }

    public int getNew_at() {
        return new_at;
    }

    public void setNew_at(int new_at) {
        this.new_at = new_at;
    }

    public boolean isFull_mail() {
        return full_mail;
    }

    public void setFull_mail(boolean full_mail) {
        this.full_mail = full_mail;
    }

    private int new_reply;
    private int new_at;
//    private int new_msg;

    private int ajax_st;
    private String ajax_code;
    private String ajax_msg;



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

    public static Creator<UserStatus> getCREATOR() {
        return CREATOR;
    }

    public String getFace_url() {
        return face_url;
    }

    public void setFace_url(String face_url) {
        this.face_url = face_url;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public boolean is_login() {
        return is_login;
    }

    public void setIs_login(boolean is_login) {
        this.is_login = is_login;
    }

    public boolean isNew_mail() {
        return new_mail;
    }

    public void setNew_mail(boolean new_mail) {
        this.new_mail = new_mail;
    }


    @Override
    public String toString() {
        return "UserStatus{" +
                "\najax_code='" + ajax_code + '\'' +
                "\n, id='" + id + '\'' +
                "\n, face_url='" + face_url + '\'' +
                "\n, is_login=" + is_login +
                "\n, new_mail=" + new_mail +
                "\n, full_mail=" + full_mail +
                "\n, new_like=" + new_like +
                "\n, new_reply=" + new_reply +
                "\n, new_at=" + new_at +
                "\n, ajax_st=" + ajax_st +
                "\n, ajax_msg='" + ajax_msg + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.face_url);
        dest.writeByte(is_login ? (byte) 1 : (byte) 0);
        dest.writeByte(new_mail ? (byte) 1 : (byte) 0);
        dest.writeByte(full_mail ? (byte) 1 : (byte) 0);
        dest.writeInt(this.new_like);
        dest.writeInt(this.new_reply);
        dest.writeInt(this.new_at);
        dest.writeInt(this.ajax_st);
        dest.writeString(this.ajax_code);
        dest.writeString(this.ajax_msg);
    }

    public UserStatus() {
    }

    protected UserStatus(Parcel in) {
        this.id = in.readString();
        this.face_url = in.readString();
        this.is_login = in.readByte() != 0;
        this.new_mail = in.readByte() != 0;
        this.full_mail = in.readByte() != 0;
        this.new_like = in.readInt();
        this.new_reply = in.readInt();
        this.new_at = in.readInt();
        this.ajax_st = in.readInt();
        this.ajax_code = in.readString();
        this.ajax_msg = in.readString();
    }

    public static final Creator<UserStatus> CREATOR = new Creator<UserStatus>() {
        @Override
        public UserStatus createFromParcel(Parcel source) {
            return new UserStatus(source);
        }

        @Override
        public UserStatus[] newArray(int size) {
            return new UserStatus[size];
        }
    };
}
