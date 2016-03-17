package com.zfdang.zsmth_android.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class for providing sample content for user interfaces created by
 * Android template wizards.
 * <p/>
 * TODO: Replace all uses of this class before publishing your app.
 */
public class BoardTopicContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Topic> ITEMS = new ArrayList<Topic>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Topic> ITEM_MAP = new HashMap<String, Topic>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createTopic(i));
        }
    }

    private static void addItem(Topic item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.getTitle(), item);
    }

    private static Topic createTopic(int position) {
        Topic topic = new Topic();
        topic.setAuthor("mozilla");
        topic.setTitle("热帖" + position);
        topic.setBoardChsName("版" + position);
        return topic;
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

}
