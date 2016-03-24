package com.zfdang.zsmth_android.models;

import java.util.ArrayList;
import java.util.List;

// this ca
/**
 * this class is shared by HotTopicFragment & BoardTopicFragment
 * Android template wizards.
 */
public class TopicListContent {


    public static final List<Topic> TOPICS = new ArrayList<Topic>();

    public static void addItem(Topic item) {
        TOPICS.add(item);
    }

    public static void clear() {
        TOPICS.clear();
    }
}
