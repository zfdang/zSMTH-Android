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
public class PostListContent {

    /**
     * An array of sample (dummy) items.
     */
    public static final List<Post> ITEMS = new ArrayList<Post>();

    /**
     * A map of sample (dummy) items, by ID.
     */
    public static final Map<String, Post> ITEM_MAP = new HashMap<String, Post>();

    private static final int COUNT = 25;

    static {
        // Add some sample items.
        for (int i = 1; i <= COUNT; i++) {
            addItem(createPost(i));
        }
    }

    private static void addItem(Post item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.getSubjectID(), item);
    }

    private static Post createPost(int position) {
        Post post = new Post();
        post.setSubjectID(String.format("Post %d", position));
        post.setTitle("hello  world");
        post.setAuthor("authoer");
        return post;

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
