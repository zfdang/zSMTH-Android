package com.zfdang.zsmth_android.models;

/**
 * Created by zfdang on 2016-3-16.
 */
public class Profile {
    private String userID;
    private String nickName;
    private String description;
    private String ip;

    private int aliveness;
    private int loginTime;
    private int postNumber;
    private int onlineStatus; // 0, 离线; 1, 未知； 2， 在线
    private int score;

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public String getUserID() {
        return userID;
    }

    public String getUserIDNickName(){
        return userID + " (" + nickName + ")";
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getNickName() {
        return nickName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }

    public void setAliveness(int aliveness) {
        this.aliveness = aliveness;
    }

    public int getAliveness() {
        return aliveness;
    }

    public void setLoginTime(int loginTime) {
        this.loginTime = loginTime;
    }

    public int getLoginTime() {
        return loginTime;
    }

    public void setPostNumber(int postNumber) {
        this.postNumber = postNumber;
    }

    public int getPostNumber() {
        return postNumber;
    }

    public void setOnlineStatus(int onlineStatus) {
        this.onlineStatus = onlineStatus;
    }

    public int getOnlineStatus() {
        return onlineStatus;
    }

    public void setScore(int score) {
        this.score = score;
    }

    public int getScore() {
        return score;
    }

}
