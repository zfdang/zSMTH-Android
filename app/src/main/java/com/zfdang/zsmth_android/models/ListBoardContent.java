package com.zfdang.zsmth_android.models;

import java.util.ArrayList;
import java.util.List;

/**
 * this class is used by FavoriteRecylerViewAdapter & AllBoardRecylerViewAdapter
 */
public class ListBoardContent {

    // used by FavoriteBoardFragment
    public static final List<Board> FAVORITE_BOARDS = new ArrayList<Board>();

    // used by AllBoardFragment
    public static final List<Board> ALL_BOARDS = new ArrayList<Board>();

    public static void addFavoriteItem(Board item) {
        FAVORITE_BOARDS.add(item);
    }

    public static void addAllBoardItem(Board item) {
        ALL_BOARDS.add(item);
    }

    public static void clearFavorites() {
        FAVORITE_BOARDS.clear();
    }

    public static void clearAllBoards() {
        ALL_BOARDS.clear();
    }

}
