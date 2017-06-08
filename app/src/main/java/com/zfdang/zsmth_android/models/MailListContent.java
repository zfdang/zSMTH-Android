package com.zfdang.zsmth_android.models;

import java.util.ArrayList;
import java.util.List;

public class MailListContent {

  public static int totalPages = 1;
  public static int totalMails = 0;
  public static int MailNumberPerPage = 20;

  public static void setTotalMails(int totalMails) {
    MailListContent.totalMails = totalMails;

    if (MailListContent.totalMails % MailNumberPerPage == 0) {
      totalPages = MailListContent.totalMails / MailNumberPerPage;
    } else {
      totalPages = MailListContent.totalMails / MailNumberPerPage + 1;
    }
  }

  public static final List<Mail> MAILS = new ArrayList<Mail>();

  public static void clear() {
    MAILS.clear();
    totalMails = 0;
    totalPages = 1;
  }

  public static void addItem(Mail item) {
    MAILS.add(item);
  }
}
