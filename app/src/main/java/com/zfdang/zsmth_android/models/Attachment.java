package com.zfdang.zsmth_android.models;

/**
 * Created by zfdang on 2016-3-16.
 */
public class Attachment {
    private int bid;
    private int id;
    private int ftype;
    private int num;
    private boolean cacheable;
    private String name;
    private int len;
    private int pos;
    private boolean isMobileType;
    private String mobileUrlString;

    private int getIntValue(String contentString) {
        return Integer.parseInt(contentString);
    }

    public String getAttachUrl() {
        if (isMobileType) {
            return mobileUrlString;
        }
        if (bid < 0) {
            return "#";
        }
        int o = name.lastIndexOf(".");
        String ext = "";
        if (o != -1) {
            ext = name.substring(o + 1).toLowerCase();
        }
        String url = "att.php?";
        if (!cacheable) {
            url += "n";
        } else if (len > 51200) {
            url += "p";
        } else {
            url += "s";
        }
        url += "." + bid + "." + id;
        if (ftype != 0) {
            url += "." + ftype + "." + num;
        }
        url += "." + pos;
        if (ext.length() >= 1) {
            url += "." + ext; // TODO: ext need htmlize
        }
        url = "http://att.newsmth.net/" + url;
        return url;
    }

    public int getBid() {
        return bid;
    }

    public void setBid(String bid) {
        this.bid = getIntValue(bid);
    }

    public int getId() {
        return id;
    }

    public void setId(String id) {
        this.id = getIntValue(id);
    }

    public int getFtype() {
        return ftype;
    }

    public void setFtype(String ftype) {
        this.ftype = getIntValue(ftype);
    }

    public int getNum() {
        return num;
    }

    public void setNum(String num) {
        this.num = getIntValue(num);
    }

    public boolean getCacheable() {
        return cacheable;
    }

    public void setCacheable(String cacheable) {
        int value = getIntValue(cacheable);
        if (value == 0) {
            this.cacheable = false;
        } else {
            this.cacheable = true;
        }
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getLen() {
        return len;
    }

    public void setLen(String len) {
        this.len = getIntValue(len);
    }

    public int getPos() {
        return pos;
    }

    public void setPos(String pos) {
        this.pos = getIntValue(pos);
    }

    public boolean isMobileType() {
        return isMobileType;
    }

    public void setMobileType(boolean isMobileType) {
        this.isMobileType = isMobileType;
    }

    public String getMobileUrlString() {
        return mobileUrlString;
    }

    public void setMobileUrlString(String mobileUrlString) {
        this.mobileUrlString = mobileUrlString;
    }
}
