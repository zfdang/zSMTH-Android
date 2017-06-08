package com.zfdang.zsmth_android.models;

import java.util.ArrayList;
import java.util.List;

// this ca

/**
 * this class is shared by HotTopicFragment & BoardTopicFragment
 * Android template wizards.
 */
public class TopicListContent {

  public static final List<Topic> HOT_TOPICS = new ArrayList<Topic>();

  public static void addHotTopic(Topic item) {
    HOT_TOPICS.add(item);
  }

  public static void clearHotTopics() {
    HOT_TOPICS.clear();
  }

  public static final List<Topic> BOARD_TOPICS = new ArrayList<Topic>();

  public static void addBoardTopic(Topic item, String boardName) {
    BOARD_TOPICS.add(item);
  }

  public static void clearBoardTopics() {
    BOARD_TOPICS.clear();
  }
}
