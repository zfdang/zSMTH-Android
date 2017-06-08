package com.zfdang.zsmth_android.models;

/**
 * Created by zfdang on 2016-3-24.
 */
public class BoardSection {
  public String sectionURL;
  public String sectionName;
  public String parentName;

  public String getBoardCategory() {
    if (parentName == null || parentName.length() == 0) {
      return sectionName;
    } else {
      return String.format("%s %s", parentName, sectionName);
    }
  }
}
