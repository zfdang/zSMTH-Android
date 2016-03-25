package com.zfdang.zsmth_android.models;

import android.util.Log;

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
    public static String BOARD_NAME = "";
    public static void setBoardName(String name) {
        BOARD_NAME = name;
        Log.d("", "Update BOARD_NAME to " + BOARD_NAME);
    }
    public static void addBoardTopic(Topic item, String boardName) {
        if(BOARD_NAME == boardName) {
            BOARD_TOPICS.add(item);
        } else {
            Log.d("TopicListContent", String.format("inconcistent board name {%s} v.s. {%s}", BOARD_NAME, boardName));
        }
    }
    public static void clearBoardTopics() {
//        BOARD_NAME = "";
        BOARD_TOPICS.clear();
    }

}
