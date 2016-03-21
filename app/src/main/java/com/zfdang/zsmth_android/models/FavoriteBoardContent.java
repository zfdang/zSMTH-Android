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
public class FavoriteBoardContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Board> ITEMS = new ArrayList<Board>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Board> ITEM_MAP = new HashMap<String, Board>();

    private static final int COUNT = 25;

    public static void addItem(Board item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.getBoardID(), item);
    }

    public static void clear() {
        ITEMS.clear();
        ITEM_MAP.clear();
    }

}
