package com.zfdang.zsmth_android.models;

public class Like {
    public String score;
    public String user;
    public String msg;
    public String time;

    public Like(String score, String user, String msg, String time) {
        this.score = score;
        this.user = user;
        this.msg = msg;
        this.time = time;

        // expand empty score to 2 spaces
        this.score = this.score.replace("[", "").replace("]", "");
        if(this.score.equals(" ")) this.score = "&nbsp;&nbsp;";

        this.user = this.user.replace(":", "");
        this.time = this.time.replace("(", "").replace(")", "");
    }

    @Override
    public String toString() {
        return "Like{" +
                "score='" + score + '\'' +
                ", user='" + user + '\'' +
                ", msg='" + msg + '\'' +
                ", time='" + time + '\'' +
                '}';
    }
}
