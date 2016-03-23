package com.zfdang.zsmth_android.models;

import java.util.ArrayList;
import java.util.List;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class GuidanceContent {

    public static final List<Topic> TOPICS = new ArrayList<Topic>();


    public static void addItem(Topic item) {
        TOPICS.add(item);
    }

    public static void clear() {
        TOPICS.clear();
    }
}
