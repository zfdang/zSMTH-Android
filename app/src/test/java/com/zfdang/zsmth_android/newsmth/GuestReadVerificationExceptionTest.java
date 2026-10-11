package com.zfdang.zsmth_android.newsmth;

import org.jsoup.Jsoup;
import org.junit.Test;

public class GuestReadVerificationExceptionTest {
  @Test(expected = GuestReadVerificationException.class)
  public void rejectsForumGuestChallengeInsteadOfTreatingItAsAnEmptyThread() {
    GuestReadVerificationException.check(Jsoup.parse(
        "<div class='guest-read-challenge'><h2>Read verification</h2>"
            + "<form>Please input the code to continue.<input class='text'></form></div>"));
  }

  @Test
  public void allowsReadableArticleContent() {
    GuestReadVerificationException.check(Jsoup.parse(
        "<table class='article'><tr><td class='a-content'>帖子正文</td></tr></table>"));
  }

  @Test
  public void doesNotMistakeVerificationWordsInAPostForTheForumChallenge() {
    GuestReadVerificationException.check(Jsoup.parse(
        "<table class='article'><tr><td class='a-content'>Read verification</td></tr></table>"));
  }
}
