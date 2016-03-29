package com.zfdang.zsmth_android.models;

/**
 * Created by zfdang on 2016-3-16.
 */
public class Attachment {
    public static int ATTACHMENT_TYPE_IMAGE = 1;
    public static int ATTACHMENT_TYPE_DOWNLOADABLE = 2;
    private String imageSrc;
    private int type;

    public Attachment(String imageSrc) {
        this.imageSrc = imageSrc;
    }
    public Attachment() {
    }

    public String getImageSrc() {
        return imageSrc;
    }

    public void setImageSrc(String imageSrc) {
        this.imageSrc = imageSrc;
    }
}
