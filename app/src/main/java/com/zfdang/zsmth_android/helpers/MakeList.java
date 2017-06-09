package com.zfdang.zsmth_android.helpers;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by zhengfa on 09/06/2017.
 */

public class MakeList {
  public static <E> List<E> makeList(Iterable<E> iter) {
    List<E> list = new ArrayList<E>();
    for (E item : iter) {
      list.add(item);
    }
    return list;
  }
}
