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
import com.zfdang.zsmth_android.models.BoardListContent;
import com.zfdang.zsmth_android.models.Topic;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.OkHttpClient;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import rx.Observable;
import rx.functions.Func1;

/**
 * Created by zfdang on 2016-3-16.
 */
public class SMTHHelper {

    static final private String TAG = "SMTHHelper";
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

    // All boards cache file
    static private final String ALL_BOARD_CACHE_FILE = "SMTH_ALL_BOARDS_CACHE";

    // singleton
    private static SMTHHelper instance = null;

    public static SMTHHelper getInstance() {
        if(instance == null) {
            instance = new SMTHHelper(SMTHApplication.getAppContext());
        }
        return instance;
    }

    // response from WWW is GB2312, we need to conver it to UTF-8
    public static String DecodeResponseFromWWW(byte[] bytes) {
        String result = null;
        try {
            result = new String(bytes, SMTH_WWW_ENCODING);
        } catch (UnsupportedEncodingException e) {
            Log.d("DecodeResponseFromWWW", e.toString());
        }
        return result;
    }

    // protected constructor, can only be called by getInstance
    protected SMTHHelper(Context context) {

        // set your desired log level
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.BASIC);

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
        List<Topic> results = new ArrayList<>();
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
        List<Topic> results = new ArrayList<>();
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

    // parse board topics from mobile
    public static List<Topic> ParseBoardTopicsFromMobile(String content) {
        List<Topic> results = new ArrayList<>();
        if (content == null) {
            return results;
        }

        Log.d("ParseBoardTopics", content);

        // <a class="plant">1/1272</a> 当前页/总共页
        Pattern pagePattern = Pattern.compile("<a class=\"plant\">(\\d+)/(\\d+)");
        Matcher pageMatcher = pagePattern.matcher(content);
        if (pageMatcher.find()) {
            int currentPageNo = Integer.parseInt(pageMatcher.group(1));
            int totalPageNo = Integer.parseInt(pageMatcher.group(2));
            Log.d("ParseBoardTopics", String.format(" %d of %d", currentPageNo, totalPageNo));
        }

//        <ul class="list sec">
//        <li class="hla"><div><a href="/article/DSLR/1700440" class="top">[合集] 水木上的低价单反广告不可信</a>(0)</div><div>2012-10-16&nbsp;<a href="/user/query/yuningilike">yuningilike</a>|2012-10-16&nbsp;<a href="/user/query/yuningilike">yuningilike</a></div></li>
//        <li><div><a href="/article/DSLR/808676907" class="m">谷歌完全免费化专业PS滤镜套装Nik Collection</a>(6)</div><div>09:52:33&nbsp;<a href="/user/query/BEO">BEO</a>|14:22:58&nbsp;<a href="/user/query/yuningilike">yuningilike</a></div></li>
//        </ul>

        // parse topics using Jsoup
        Document doc = Jsoup.parse(content);

        // get all lis
        Elements lis = doc.select("ul li");
        for (Element li: lis) {
            Log.d("ParseBoardTopics", li.toString());
            Topic topic = new Topic();

            Elements links = li.select("a[href]");
            if(links.size() == 3) {
                Element link1 =  links.get(0);
                Element link2 =  links.get(1);
                Element link3 =  links.get(2);
                String topicID = ParseTopicID(link1.attr("href"));
                String title = link1.text();
                String author = link2.text();
                String replier = link3.text();

                topic.setAuthor(author);
                topic.setTopicID(topicID);
                topic.setTitle(title);
                topic.setReplier(replier);
            }

            // find dates
            Elements divs = li.select("div");
            if(divs.size() == 2) {
                String temp = divs.get(1).text();
                // temp的样本
                // 2016-03-22 Dd1122Ee|2016-03-23 DRAGON94Dd1122Ee
                // 09:51:35 Qid|11:37:42 Frankiewong4Qid
                String[] tokens = temp.split("[\\|\\s]+");
                if(tokens.length == 4) {
                    String publishDate = tokens[0];
                    String replyDate = tokens[2];

                    topic.setPublishDate(publishDate);
                    topic.setReplyDate(replyDate);
                }
            }

            Log.d("ParseBoardTopics", topic.toString());
            results.add(topic);
        }


        return results;
    }


    public static String ParseTopicID(String temp) {
        Pattern pattern = Pattern.compile("/article/(\\w+)/(\\d+)");
        Matcher matcher = pattern.matcher(temp);
        if (matcher.find()) {
//            String boardName = matcher.group(1);
            String topicID = matcher.group(2);
            return topicID;
        }
        return "";
    }


    public static List<Board> ParseFavoriteBoardsFromWWW(String content) {
        List<Board> boards = new ArrayList<>();

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

    /*
    * All Boards related methods
    * Starts here
     */

    public static List<Board> LoadAllBoardFromCache(){
        List<Board> boards = new ArrayList<>();
        try {
            FileInputStream is = SMTHApplication.getAppContext().openFileInput(ALL_BOARD_CACHE_FILE);
            ObjectInputStream ois = new ObjectInputStream(is);
            boards = (ArrayList<Board>) ois.readObject();
            is.close();
            Log.d("LoadAllBoardFromCache", String.format("%d boards loaded from cache file", boards.size()));
        } catch (Exception e) {
            Log.d("LoadAllBoardFromCache", e.toString());
            Log.d("LoadAllBoardFromCache", "failed to load boards from cache");
        }
        return boards;
    }

    public static void SaveAllBoardToCache(List<Board> boards){
        try {
            FileOutputStream fos = SMTHApplication.getAppContext().openFileOutput(ALL_BOARD_CACHE_FILE, Context.MODE_PRIVATE);
            ObjectOutputStream os = new ObjectOutputStream(fos);
            os.writeObject(boards);
            fos.close();
            Log.d("SaveAllBoardToCache", String.format("%d boards saved to cache file", boards.size()));
        } catch (Exception e) {
            Log.d("SaveAllBoardToCache", e.toString());
            Log.d("SaveAllBoardToCache", "failed to save boards to cache file");
        }
    }

    public static void ClearAllBoardCache() {
        try{
            if(SMTHApplication.getAppContext().deleteFile(ALL_BOARD_CACHE_FILE))
            {
                Log.d("ClearAllBoardCache", "delete all_boards cache file successfully");
                return;
            }
        } catch (Exception e) {
            Log.d("ClearAllBoardCache", e.toString());
        }
        Log.d("ClearAllBoardCache", "Failed to delete all_boards cache file");
    }

    // load all boards from WWW, recursively
    // http://stackoverflow.com/questions/31246088/how-to-do-recursive-observable-call-in-rxjava
    public static List<Board> LoadAllBoardsFromWWW() {
        final String[] SectionNames = {"社区管理", "国内院校", "休闲娱乐", "五湖四海", "游戏运动", "社会信息", "知性感性", "文化人文", "学术科学", "电脑技术", "终止版面"};
        final String[] SectionURLs = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "A"};
        final List<BoardSection> sections = new ArrayList<>();
        for(int index = 0; index < SectionNames.length; index ++) {
            BoardSection section = new BoardSection();
            section.sectionURL = SectionURLs[index];
            section.sectionName = SectionNames[index];
            sections.add(section);
        }

        List<Board>  boards = Observable.from(sections)
                .flatMap(new Func1<BoardSection, Observable<Board>>() {
                    @Override
                    public Observable<Board> call(BoardSection section) {
                        return SMTHHelper.loadBoardsInSectionFromWWW(section);
                    }
                })
                .flatMap(new Func1<Board, Observable<Board>>() {
                    @Override
                    public Observable<Board> call(Board board) {
                        return SMTHHelper.loadChildBoardsRecursivelyFromWWW(board);
                    }
                })
                .filter(new Func1<Board, Boolean>() {
                    @Override
                    public Boolean call(Board board) {
                        // keep board only
                        return !board.isFolder();
                    }
                })
                // http://stackoverflow.com/questions/26311513/convert-observable-to-list
                .toList().toBlocking().single();

        // sort the board list by chinese name
        Collections.sort(boards, new BoardListContent.ChineseComparator());
        Log.d("LoadAllBoardsFromWWW", String.format("%d boards loaded from network", boards.size()));

        // save boards to disk
        SaveAllBoardToCache(boards);

        return boards;
    }

    public static Observable<Board> loadChildBoardsRecursivelyFromWWW(Board board) {
        if(board.isFolder()) {
            BoardSection section = new BoardSection();
            section.sectionURL = board.getFolderID();
            section.sectionName = board.getFolderName();
            section.parentName = board.getCategoryName();

            return SMTHHelper.loadBoardsInSectionFromWWW(section);
        } else {
            return Observable.just(board);
        }
    }


    public static Observable<Board> loadBoardsInSectionFromWWW(final BoardSection section) {
        String sectionURL = section.sectionURL;
        return SMTHHelper.getInstance().wService.getBoardsBySection(sectionURL)
                .flatMap(new Func1<ResponseBody, Observable<Board>>() {
                    @Override
                    public Observable<Board> call(ResponseBody responseBody) {
                        try {
                            String response = responseBody.string();
                            List<Board> boards = SMTHHelper.ParseBoardsInSectionFromWWW(response, section);
                            return Observable.from(boards);

                        } catch (Exception e) {
                            Log.d(TAG, e.toString());
                            return null;
                        }
                    }
                });
    }



    public static List<Board> ParseBoardsInSectionFromWWW(String content, BoardSection section) {
        List<Board> boards = new ArrayList<>();

//        <tr><td class="title_1"><a href="/nForum/section/Association">协会社团</a><br />Association</td><td class="title_2">[二级目录]<br /></td><td class="title_3">&nbsp;</td><td class="title_4 middle c63f">&nbsp;</td><td class="title_5 middle c09f">&nbsp;</td><td class="title_6 middle c63f">&nbsp;</td><td class="title_7 middle c09f">&nbsp;</td></tr>
//        <tr><td class="title_1"><a href="/nForum/board/BIT">北京理工大学</a><br />BIT</td><td class="title_2"><a href="/nForum/user/query/mahenry">mahenry</a><br /></td><td class="title_3"><a href="/nForum/article/BIT/250116">今年几万斤苹果都滞销了，果农欲哭无泪！</a><br />发贴人:&ensp;jingling6787 日期:&ensp;2016-03-22 09:19:09</td><td class="title_4 middle c63f">11</td><td class="title_5 middle c09f">2</td><td class="title_6 middle c63f">5529</td><td class="title_7 middle c09f">11854</td></tr>
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
                    board.setCategoryName(section.getBoardCategory());
                    boards.add(board);

                }

                Pattern sectionPattern = Pattern.compile("/nForum/section/(\\w+)");
                Matcher sectionMatcher = sectionPattern.matcher(temp);
                if (sectionMatcher.find()) {
                    // it's a section
                    folderEngName = sectionMatcher.group(1);
                    folderChsName = link1.text();

                    Board board = new Board(folderEngName, folderChsName);
                    board.setCategoryName(section.sectionName);
                    boards.add(board);
                }

//                Log.d("parse", String.format("%s, %s, %s, %s, %s", chsBoardName, engBoardName, folderChsName, folderEngName, moderator));
            }

        }

        return boards;
    }
    /*
    * All Boards related methods
    * Ends here
     */

}
