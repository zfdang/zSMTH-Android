package com.zfdang.zsmth_android.models;

/**
 * Created by zfdang on 2016-3-14.
 */
public class Board {

    private String boardID;
    private String boardEngName;
    private String boardChsName;
    private String categoryName;
    private String moderator;

    // 可能是个版面的目录，而不是具体的版面
    private boolean isFolder;
    private String folderName;
    private String folderID;


    public Board(String id, String chsName, String enName) {
        isFolder = false;
        this.boardID = id;
        this.boardChsName = chsName;
        this.boardEngName = enName;
    }


    public Board(String folderID, String folderName) {
        isFolder = true;
        this.folderID = folderID;
        this.folderName = folderName;
        this.categoryName = "目录";
    }

    public String getBoardID() {
        return boardID;
    }

    public String getBoardEngName() {
        return boardEngName;
    }

    public String getBoardChsName() {
        return boardChsName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public String getModerator() {
        return moderator;
    }

    public boolean isFolder() {
        return isFolder;
    }

    public String getFolderName() {
        return folderName;
    }

    public String getFolderID() {
        return folderID;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public void setModerator(String moderator) {
        this.moderator = moderator;
    }

    @Override
    public String toString() {
        return "Board{" +
                "boardID='" + boardID + '\'' +
                ", boardEngName='" + boardEngName + '\'' +
                ", boardChsName='" + boardChsName + '\'' +
                ", categoryName='" + categoryName + '\'' +
                ", moderator='" + moderator + '\'' +
                ", isFolder=" + isFolder +
                ", folderName='" + folderName + '\'' +
                ", folderID='" + folderID + '\'' +
                '}';
    }
}
