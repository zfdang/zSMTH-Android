package com.zfdang.zsmth_android.listeners;

import com.zfdang.zsmth_android.models.Board;

/**
 * Created by zfdang on 2016-3-22.
 * shared by FavoriteBoardFragment & AllBoardFragment, so we move it out
 */
public interface OnBoardFragmentInteractionListener {

  void onBoardFragmentInteraction(Board item);

  void onBoardLongClick(Board item);
}
