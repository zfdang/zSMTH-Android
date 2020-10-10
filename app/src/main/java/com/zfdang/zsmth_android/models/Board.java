package com.zfdang.zsmth_android.models;

import android.os.Parcel;
import android.os.Parcelable;

import java.io.Externalizable;
import java.io.IOException;
import java.io.ObjectInput;
import java.io.ObjectOutput;

/**
 * Created by zfdang on 2016-3-14.
 */
public class Board implements Externalizable, Parcelable {
  // for Externalizable
  static final long serialVersionUID = 20160322L;

  public enum BoardType {
    BOARD,
    SECTION,
    FOLDER,
    INVALID
  }
  BoardType boardType;

  // 具体的版面
  // http://www.newsmth.net/nForum/#!board/DrivingStudy
  private String boardID;       // this field is not longer used in nForum
  private String boardEngName;  // Android
  private String boardChsName;  // 安卓系统设备

  private String categoryName;  // 电脑技术
  private String moderator;     // 版主

  // 用户在收藏夹里创建的目录
  // folderId = 1, folderName = 次常用版面
  private String folderID;      // 1
  private String folderName;    // 次常用版面

  // 系统的二级目录
  // o.o(true,1,678,59144,'[供求]','SecondHand','二手货交易','[目录]',981,677,0);
  // http://www.newsmth.net/nForum/#!section/Automobile
  private String sectionID;    // SecondHand
  private String sectionName;  // 二手货交易
  public String sectionPath;

  // http://stackoverflow.com/questions/21966784/reading-object-from-file-throws-illegalaccessexception
  // used by readObject
  public Board() {
  }

  public void initAsInvalid(String message) {
    boardType = BoardType.INVALID;
    this.boardEngName = message;
    this.categoryName = "错误提示";
  }


  public void initAsBoard(String boardEngName, String boardChsName, String categoryName, String moderator){
    boardType = BoardType.BOARD;

    this.boardEngName = boardEngName;
    this.boardChsName = boardChsName;
    this.categoryName = categoryName;
    if (moderator.length() > 25) {
      moderator = moderator.substring(0, 21) + "...";
    }
    this.moderator = moderator;
  }


  public void initAsFolder(String folderID, String folderName){
    boardType = BoardType.FOLDER;
    this.folderID = folderID;
    this.folderName = folderName;
    this.categoryName = "自定义目录";
  }

  public void initAsSection(String sectionID, String sectionName){
    boardType = BoardType.SECTION;
    this.sectionID = sectionID;
    this.sectionName = sectionName;
    this.categoryName = "二级目录";
  }

  public boolean isBoard(){
    return boardType == BoardType.BOARD;
  }

  public boolean isFolder() {
    return boardType == BoardType.FOLDER;
  }

  public boolean isSection() {
    return boardType == BoardType.SECTION;
  }

  public boolean isInvalid() {
    return boardType == BoardType.INVALID;
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

  public String getFolderID() {
    return folderID;
  }

  public String getFolderName() {
    return folderName;
  }

  public String getSectionID() {
    return sectionID;
  }

  public String getSectionName() {
    return sectionName;
  }

  public String getBoardName() {
    if (boardChsName == null || boardChsName.length() == 0) {
      return boardEngName;
    } else {
      return String.format("[%s] %s", boardEngName, boardChsName);
    }
  }

  @Override
  public String toString() {
    return "Board{" +
            "boardType=" + boardType +
            ", boardID='" + boardID + '\'' +
            ", boardEngName='" + boardEngName + '\'' +
            ", boardChsName='" + boardChsName + '\'' +
            ", categoryName='" + categoryName + '\'' +
            ", moderator='" + moderator + '\'' +
            ", folderID='" + folderID + '\'' +
            ", folderName='" + folderName + '\'' +
            ", sectionID='" + sectionID + '\'' +
            ", sectionName='" + sectionName + '\'' +
            ", sectionPath='" + sectionPath + '\'' +
            '}';
  }


  @Override
  public int describeContents() {
    return 0;
  }

  @Override
  public void writeToParcel(Parcel dest, int flags) {
    dest.writeInt(this.boardType == null ? -1 : this.boardType.ordinal());
    dest.writeString(this.boardID);
    dest.writeString(this.boardEngName);
    dest.writeString(this.boardChsName);
    dest.writeString(this.categoryName);
    dest.writeString(this.moderator);
    dest.writeString(this.folderID);
    dest.writeString(this.folderName);
    dest.writeString(this.sectionID);
    dest.writeString(this.sectionName);
    dest.writeString(this.sectionPath);
  }

  protected Board(Parcel in) {
    int tmpBoardType = in.readInt();
    this.boardType = tmpBoardType == -1 ? null : BoardType.values()[tmpBoardType];
    this.boardID = in.readString();
    this.boardEngName = in.readString();
    this.boardChsName = in.readString();
    this.categoryName = in.readString();
    this.moderator = in.readString();
    this.folderID = in.readString();
    this.folderName = in.readString();
    this.sectionID = in.readString();
    this.sectionName = in.readString();
    this.sectionPath = in.readString();
  }

  public static final Creator<Board> CREATOR = new Creator<Board>() {
    @Override
    public Board createFromParcel(Parcel source) {
      return new Board(source);
    }

    @Override
    public Board[] newArray(int size) {
      return new Board[size];
    }
  };

  @Override
  public void readExternal(ObjectInput in) throws IOException, ClassNotFoundException {
    if (in.readBoolean()) {
      boardType = BoardType.values()[in.readShort()];
    }
    if (in.readBoolean()) {
      boardID = in.readUTF();
    }
    if (in.readBoolean()) {
      boardEngName = in.readUTF();
    }
    if (in.readBoolean()) {
      boardChsName = in.readUTF();
    }
    if (in.readBoolean()) {
      categoryName = in.readUTF();
    }
    if (in.readBoolean()) {
      moderator = in.readUTF();
    }
    if (in.readBoolean()) {
      folderID = in.readUTF();
    }
    if (in.readBoolean()) {
      folderName = in.readUTF();
    }
    if (in.readBoolean()) {
      sectionID = in.readUTF();
    }
    if (in.readBoolean()) {
      sectionName = in.readUTF();
    }
    if (in.readBoolean()) {
      sectionPath = in.readUTF();
    }
  }

  @Override
  public void writeExternal(ObjectOutput out) throws IOException {
    if (boardType == null) {
      out.writeBoolean(false);
    } else {
      out.writeBoolean(true);
      out.writeShort((short) boardType.ordinal());
    }
    if (boardID == null) {
      out.writeBoolean(false);
    } else {
      out.writeBoolean(true);
      out.writeUTF(boardID);
    }
    if (boardEngName == null) {
      out.writeBoolean(false);
    } else {
      out.writeBoolean(true);
      out.writeUTF(boardEngName);
    }
    if (boardChsName == null) {
      out.writeBoolean(false);
    } else {
      out.writeBoolean(true);
      out.writeUTF(boardChsName);
    }
    if (categoryName == null) {
      out.writeBoolean(false);
    } else {
      out.writeBoolean(true);
      out.writeUTF(categoryName);
    }
    if (moderator == null) {
      out.writeBoolean(false);
    } else {
      out.writeBoolean(true);
      out.writeUTF(moderator);
    }
    if (folderID == null) {
      out.writeBoolean(false);
    } else {
      out.writeBoolean(true);
      out.writeUTF(folderID);
    }
    if (folderName == null) {
      out.writeBoolean(false);
    } else {
      out.writeBoolean(true);
      out.writeUTF(folderName);
    }
    if (sectionID == null) {
      out.writeBoolean(false);
    } else {
      out.writeBoolean(true);
      out.writeUTF(sectionID);
    }
    if (sectionName == null) {
      out.writeBoolean(false);
    } else {
      out.writeBoolean(true);
      out.writeUTF(sectionName);
    }
    if (sectionPath == null) {
      out.writeBoolean(false);
    } else {
      out.writeBoolean(true);
      out.writeUTF(sectionPath);
    }
  }



}
