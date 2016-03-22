package com.zfdang.zsmth_android.newsmth;

import android.content.Context;
import android.text.Html;
import android.util.Log;

import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.models.Board;
import com.zfdang.zsmth_android.models.Topic;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by zfdang on 2016-3-16.
 */
public class SMTHHelper {

    private OkHttpClient httpClient = null;

    // WWW service of SMTH
    private final String SMTH_WWW_URL = "http://www.newsmth.net";
    static private final String SMTH_WWW_ENCODING = "GB2312";
    private Retrofit mRetrofit = null;
    public SMTHWWWService wService = null;

    // Mobile service of SMTH
    private final String SMTH_MOBILE_URL = "http://m.newsmth.net";
    private Retrofit wRetrofit = null;
    public SMTHMobileService mService = null;

    // singleton
    private static SMTHHelper instance = null;

    public static SMTHHelper getInstance() {
        if(instance == null) {
            instance = new SMTHHelper(SMTHApplication.getAppContext());
        }
        return instance;
    }

    public static String DecodeResponseFromWWW(byte[] bytes) {
        String result = null;
        try {
            result = new String(bytes, SMTH_WWW_ENCODING);
        } catch (UnsupportedEncodingException e) {
            Log.d("DecodeResponseFromWWW", e.toString());
        }
        return result;
    }

    public static String ParseLoginResponseFromMobile(String response) {
        String result = "";
        Pattern hp = Pattern.compile("<div class=\"sp hl f\">([^<]*)</div>");
        Matcher hm = hp.matcher(response);
        if (hm.find()) {
            // add section header as special topic: category
            result = hm.group(1);
        }
        return result;
    }

    // can only be called by getInstance
    protected SMTHHelper(Context context) {

        // set your desired log level
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // https://github.com/franmontiel/PersistentCookieJar
        // A persistent CookieJar implementation for OkHttp 3 based on SharedPreferences.
        ClearableCookieJar cookieJar =
                new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));

        httpClient = new OkHttpClient().newBuilder()
                .addInterceptor(logging)
                .cookieJar(cookieJar)
                .build();

        mRetrofit = new Retrofit.Builder()
                .baseUrl(SMTH_MOBILE_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(httpClient)
                .build();
        mService = mRetrofit.create(SMTHMobileService.class);

        wRetrofit = new Retrofit.Builder()
                .baseUrl(SMTH_WWW_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .client(httpClient)
                .build();
        wService = wRetrofit.create(SMTHWWWService.class);
    }

    // parse guidance page, to find all hot topics
    // http://m.newsmth.net/hot/topTen
    // http://m.newsmth.net/hot/1
    public static List<Topic> ParseHotTopicsFromMobile(String content) {
        List<Topic> results = new ArrayList<Topic>();
        if (content == null) {
            return results;
        }

        // two possible patterns here:
        // <li class="f">国内院校热门话题</li>
        // <li>10|<a href="/article/Weiqi/552886">阿法狗（分布式）和李世石的比赛不公平(<span style="color:red">64</span>)</a></li>
        // <li>3|<a href="/article/BJTU/222066">[代挂][pic]90年高挑金融MM诚意挂，上海 (转载)</a></li>
        content = content.replaceAll("<span style=\"color:red\">", "");
        content = content.replaceAll("</span>", "");

        Pattern hp = Pattern.compile("<li class=\"f\">([^<>]*)</li>", Pattern.DOTALL);
        Matcher hm = hp.matcher(content);
        if (hm.find()) {
            // add section header as special topic: category
            String sectionTitle = hm.group(1);
            results.add(new Topic(sectionTitle));

            Pattern topic_pattern = Pattern.compile("<li>(\\d+)\\|<a href=\"/article/(\\w+)/(\\d+)\">([^<>]*)</a></li>");
            Matcher topic_search = topic_pattern.matcher(content);
            while (topic_search.find()) {
                // add hot topic
                Topic topic = new Topic();
                topic.setBoardEngName(topic_search.group(2));
                topic.setTopicID(topic_search.group(3));
                String titleString = Html.fromHtml(topic_search.group(4)).toString();
                topic.setTitle(titleString);

                results.add(topic);
            }
        }

        return results;
    }


    // parse guidance page, to find all hot topics
    public static List<Topic> ParseHotTopicsFromWWW(String content) {
        List<Topic> results = new ArrayList<Topic>();
        if (content == null) {
            return results;
        }

        Pattern hp = Pattern.compile("<div id=\"top10\">(.*?)</ul></div>", Pattern.DOTALL);
        Matcher hm = hp.matcher(content);
        if (hm.find()) {
            // add category
            results.add(new Topic("水木十大"));


            String hc = hm.group(1);
            Pattern boardNamePattern = Pattern.compile("<a href=\"bbsdoc.php\\?board=\\w+\">([^<>]+)</a>");
            Matcher boardNameMatcher = boardNamePattern.matcher(hc);

            Pattern hip = Pattern.compile("<a href=\"bbstcon.php\\?board=(\\w+)&gid=(\\d+)\">([^<>]+)</a>");
            Matcher him = hip.matcher(hc);
            Pattern hIdPattern = Pattern.compile("<a href=\"bbsqry.php\\?userid=(\\w+)\">");
            Matcher hIdMatcher = hIdPattern.matcher(hc);
            while (him.find() && hIdMatcher.find()) {
                // add hot topic
                Topic topic = new Topic();
                if (boardNameMatcher.find()) {
                    topic.setBoardChsName(boardNameMatcher.group(1));
                }
                topic.setBoardEngName(him.group(1));
                topic.setTopicID(him.group(2));
                String titleString = Html.fromHtml(him.group(3)).toString();
                topic.setTitle(titleString);
                topic.setAuthor(hIdMatcher.group(1));

                results.add(topic);
            }
        }

        Pattern sp = Pattern.compile(
                "<span class=\"SectionName\"><a[^<>]+>([^<>]+)</a></span>(.*?)class=\"SecLine\"></td>", Pattern.DOTALL);
        Matcher sm = sp.matcher(content);
        while (sm.find()) {
            String sectionName = sm.group(1);
            // add section
            results.add(new Topic(sectionName));

            String sc = sm.group(2);
            Pattern boardNamePattern = Pattern
                    .compile("\"SectionItem\">.<a href=\"bbsdoc.php\\?board=\\w+\">([^<>]+)</a>");
            Matcher boardNameMatcher = boardNamePattern.matcher(sc);

            Pattern sip = Pattern.compile("<a href=\"bbstcon.php\\?board=(\\w+)&gid=(\\d+)\">([^<>]+)</a>");
            Matcher sim = sip.matcher(sc);
            while (sim.find()) {
                Topic topic = new Topic();
                if (boardNameMatcher.find()) {
                    topic.setBoardChsName(boardNameMatcher.group(1));
                }
                topic.setBoardEngName(sim.group(1));
                topic.setTopicID(sim.group(2));
                topic.setTitle(sim.group(3));
                results.add(topic);
            }
        }

        results.add(new Topic("END."));

        return results;
    }

    public static List<Board> ParseFavoriteBoardsFromWWW(String content) {
        List<Board> boards = new ArrayList<Board>();

//        o.f(1,'favFolder1 ',0,'');
//        o.o(false,1,896,22556,'[站务]','Advice','水木发展','SYSOP',7026,895,4);
//        o.o(false,1,619,2332235,'[生活]','CouponsLife','辣妈羊毛党','hmilytt XZCL',897207,618,1601);
//        o.o(false,1,179,808676665,'[数码]','DSLR','数码单反','jerryxiao',153110,178,57);

        // 先提取目录
        Pattern pattern = Pattern.compile("o\\.f\\((\\d+),'([^']+)',\\d+,''\\);");
        Matcher matcher = pattern.matcher(content);
//        List<String> list = new ArrayList<String>();
        while (matcher.find()) {
//            list.add(matcher.group(1));
            Board board = new Board(matcher.group(1), matcher.group(2));
            boards.add(board);
        }

        // 再提取收藏的版面
        // o.o(false,1,998,22156,'[站务]','Ask','新用户疑难解答','haning BJH',733,997,0);
        pattern = Pattern.compile("o\\.o\\(\\w+,\\d+,(\\d+),\\d+,'\\[([^']+)\\]','([^']+)','([^']+)','([^']*)',\\d+,\\d+,\\d+\\)");
        matcher = pattern.matcher(content);
        while (matcher.find()) {
            String boardID = matcher.group(1);
            String category = matcher.group(2);
            String engName = matcher.group(3);
            String chsName = matcher.group(4);
            String moderator = matcher.group(5);
            if (moderator.length() > 25) {
                moderator = moderator.substring(0, 21) + "...";
            }
            Board board = new Board(boardID, chsName, engName);
            board.setModerator(moderator);
            board.setCategoryName(category);
            boards.add(board);
        }

        return boards;
    }


    public static List<Board> ParseBoardsInSectionFromWWW(String content) {
        List<Board> boards = new ArrayList<Board>();

//        <tr><td class="title_1"><a href="/nForum/section/Association">协会社团</a><br />Association</td><td class="title_2">[二级目录]<br /></td><td class="title_3">&nbsp;</td><td class="title_4 middle c63f">&nbsp;</td><td class="title_5 middle c09f">&nbsp;</td><td class="title_6 middle c63f">&nbsp;</td><td class="title_7 middle c09f">&nbsp;</td></tr>
//
//        <tr><td class="title_1"><a href="/nForum/board/BIT">北京理工大学</a><br />BIT</td><td class="title_2"><a href="/nForum/user/query/mahenry">mahenry</a><br /></td><td class="title_3"><a href="/nForum/article/BIT/250116">今年几万斤苹果都滞销了，果农欲哭无泪！</a><br />发贴人:&ensp;jingling6787 日期:&ensp;2016-03-22 09:19:09</td><td class="title_4 middle c63f">11</td><td class="title_5 middle c09f">2</td><td class="title_6 middle c63f">5529</td><td class="title_7 middle c09f">11854</td></tr>
//
//        <tr><td class="title_1"><a href="/nForum/board/Orienteering">定向越野</a><br />Orienteering</td><td class="title_2"><a href="/nForum/user/query/onceloved">onceloved</a><br /></td><td class="title_3"><a href="/nForum/article/Orienteering/59193">圆明园定向</a><br />发贴人:&ensp;jiang2000 日期:&ensp;2016-03-19 14:19:10</td><td class="title_4 middle c63f">0</td><td class="title_5 middle c09f">0</td><td class="title_6 middle c63f">4725</td><td class="title_7 middle c09f">18864</td></tr>

        Document doc = Jsoup.parse(content);
        // get all tr
        Elements trs = doc.select("table.board-list tr");
        for (Element tr: trs) {
//            Log.d("Node", tr.toString());

            Elements t1links = tr.select("td.title_1 a[href]");
            if(t1links.size() == 1) {
                Element link1 = t1links.first();
                String temp = link1.attr("href");

                String chsBoardName = "";
                String engBoardName = "";
                String moderator = "";
                String folderChsName = "";
                String folderEngName = "";

                Pattern boardPattern = Pattern.compile("/nForum/board/(\\w+)");
                Matcher boardMatcher = boardPattern.matcher(temp);
                if (boardMatcher.find()) {
                    engBoardName = boardMatcher.group(1);
                    chsBoardName = link1.text();
                    // it's a normal board
                    Elements t2links = tr.select("td.title_2 a[href]");
                    if(t2links.size() == 1 ) {
                        // if we can find link to moderator, set moderator
                        // it's also possible that moderator is empty, so no link can be found
                        Element link2 = t2links.first();
                        moderator = link2.text();
                    }

                    Board board = new Board("", chsBoardName, engBoardName);
                    board.setModerator(moderator);
                    boards.add(board);

                }

                Pattern sectionPattern = Pattern.compile("/nForum/section/(\\w+)");
                Matcher sectionMatcher = sectionPattern.matcher(temp);
                if (sectionMatcher.find()) {
                    // it's a section
                    folderEngName = sectionMatcher.group(1);
                    folderChsName = link1.text();

                    Board board = new Board(folderEngName, folderChsName);
                    boards.add(board);
                }

                Log.d("parse", String.format("%s, %s, %s, %s, %s", chsBoardName, engBoardName, folderChsName, folderEngName, moderator));
            }

        }

        return boards;
    }

}
