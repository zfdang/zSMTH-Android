package com.zfdang.zsmth_android.models;

import java.text.Collator;
import java.text.RuleBasedCollator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * this class is used by FavoriteRecylerViewAdapter & AllBoardRecylerViewAdapter
 */
public class ListBoardContent {

    // used by FavoriteBoardFragment
    public static final List<Board> FAVORITE_BOARDS = new ArrayList<>();

    // used by AllBoardFragment
    public static final List<Board> ALL_BOARDS = new ArrayList<>();

    public static void addFavoriteItem(Board item) {
        FAVORITE_BOARDS.add(item);
    }

    public static void addAllBoardItem(Board item) {
        ALL_BOARDS.add(item);
    }

    public static class ChineseComparator implements Comparator<Board> {
        RuleBasedCollator collator = (RuleBasedCollator) Collator.getInstance(Locale.CHINA);
        public int compare(Board b1, Board b2) {
            return collator.compare(b1.getBoardChsName(), b2.getBoardChsName());
        }
    }


    public static void sortAllBoardItem() {
        // remove pure sections, and only boards are kept
//        List<Board> temp = new ArrayList<>(ALL_BOARDS);
//        for (Board board: temp ) {
//            if(board.getBoardChsName() == null || board.getBoardEngName() == null) {
//                Log.d("ListBoardContent", board.toString());
//            }
//            if(board.isFolder()) {
//                ALL_BOARDS.remove(board);
//            }
//        }

        // sort boards by chinese name
        Collections.sort(ALL_BOARDS, new ChineseComparator());
    }

    public static void clearFavorites() {
        FAVORITE_BOARDS.clear();
    }

    public static void clearAllBoards() {
        ALL_BOARDS.clear();
    }

}
