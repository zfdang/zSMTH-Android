package com.zfdang.zsmth_android.models;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;
import java.util.ArrayList;

/**
 * Created by zfdang on 2016-3-14.
 */
public class Board implements Externalizable{

    private String boardID;
    private String boardEngName;
    private String boardChsName;
    private String categoryName;
    private String moderator;
    private boolean isDirectory;
    private String directoryName;
    private int currentPageNo;
    private int totalPageNo;
    private ArrayList<Board> childBoards = new ArrayList<Board>();


    public Board(String id, String chsName, String enName) {
        this.boardID = id;
        this.boardChsName = chsName;
        this.boardEngName = enName;
    }


    @Override
    public String toString() {
        return this.boardEngName + "\t" + this.boardChsName;
    }

    public String getBoardID() {
        return boardID;
    }

    public void setBoardID(String boardID) {
        this.boardID = boardID;
    }

    public String getEngName() {
        return boardEngName;
    }

    public void setEngName(String name) {
        this.boardEngName = name;
    }

    public String getChsName() {
        return boardChsName;
    }

    public void setChsName(String cname) {
        this.boardChsName = cname;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setModerator(String moderator) {
        this.moderator = moderator;
    }

    public String getModerator() {
        return moderator;
    }

    public void setDirectory(boolean isDirectory) {
        this.isDirectory = isDirectory;
    }

    public boolean isDirectory() {
        return isDirectory;
    }

    public void setDirectoryName(String directoryName) {
        this.directoryName = directoryName;
    }

    public String getDirectoryName() {
        return directoryName;
    }

    public void setCurrentPageNo(int currentPage) {
        this.currentPageNo = currentPage;
    }

    public int getCurrentPageNo() {
        return currentPageNo;
    }

    public void setTotalPageNo(int totalPageNo) {
        this.totalPageNo = totalPageNo;
    }

    public int getTotalPageNo() {
        return totalPageNo;
    }

    public void setChildBoards(ArrayList<Board> childBoards) {
        this.childBoards = childBoards;
    }

    public ArrayList<Board> getChildBoards() {
        return childBoards;
    }

    @SuppressWarnings("unchecked")
    @Override
    public void readExternal(ObjectInput input) throws IOException,
            ClassNotFoundException {
        boardID = (String) input.readObject();
        boardEngName = (String) input.readObject();
        boardChsName = (String) input.readObject();
        categoryName = (String) input.readObject();
        moderator = (String) input.readObject();
        isDirectory = input.readBoolean();
        directoryName = (String) input.readObject();
        currentPageNo = input.readInt();
        totalPageNo = input.readInt();
        childBoards = (ArrayList<Board>) input.readObject();
    }

    @Override
    public void writeExternal(ObjectOutput output) throws IOException {
        output.writeObject(boardID);
        output.writeObject(boardEngName);
        output.writeObject(boardChsName);
        output.writeObject(categoryName);
        output.writeObject(moderator);
        output.writeBoolean(isDirectory);
        output.writeObject(directoryName);
        output.writeInt(currentPageNo);
        output.writeInt(totalPageNo);
        output.writeObject(childBoards);
    }


}
