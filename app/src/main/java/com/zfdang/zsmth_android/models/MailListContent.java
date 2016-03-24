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
public class MailListContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Mail> ITEMS = new ArrayList<Mail>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Mail> ITEM_MAP = new HashMap<String, Mail>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createMail(i));
        }
    }

    private static void addItem(Mail item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static Mail createMail(int position) {
        return new Mail(String.valueOf(position), "Mail " + position, makeDetails(position));
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Mail: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }


}
