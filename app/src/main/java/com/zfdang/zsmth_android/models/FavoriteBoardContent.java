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

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDummyItem(i));
        }
    }

    private static void addItem(Board item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.getBoardID(), item);
    }

    private static Board createDummyItem(int position) {
        Board board = new Board(String.valueOf(position), "chsName", "engBoardName");
        board.setModerator("版主");
        board.setCategoryName("休闲娱乐");
        return board;
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about favorite: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }
}
