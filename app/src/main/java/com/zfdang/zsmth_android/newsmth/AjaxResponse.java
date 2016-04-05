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


public class AjaxResponse implements Parcelable {
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

    private int ajax_st;
    private String ajax_code;
    private String ajax_msg;

    @Override
    public String toString() {
        return "AjaxResponse{" +
                "ajax_code='" + ajax_code + '\'' +
                ", ajax_st=" + ajax_st +
                ", ajax_msg='" + ajax_msg + '\'' +
                '}';
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(this.ajax_st);
        dest.writeString(this.ajax_code);
        dest.writeString(this.ajax_msg);
    }

    public AjaxResponse() {
    }

    protected AjaxResponse(Parcel in) {
        this.ajax_st = in.readInt();
        this.ajax_code = in.readString();
        this.ajax_msg = in.readString();
    }

    public static final Parcelable.Creator<AjaxResponse> CREATOR = new Parcelable.Creator<AjaxResponse>() {
        @Override
        public AjaxResponse createFromParcel(Parcel source) {
            return new AjaxResponse(source);
        }

        @Override
        public AjaxResponse[] newArray(int size) {
            return new AjaxResponse[size];
        }
    };
}
