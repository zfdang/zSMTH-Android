package com.zfdang.zsmth_android.models;

import java.util.ArrayList;
import java.util.List;

public class MailListContent {

    public static final List<Mail> MAILS = new ArrayList<Mail>();

    public static void clear() {
        MAILS.clear();
    }

    public static void addItem(Mail item) {
        MAILS.add(item);
    }
}
