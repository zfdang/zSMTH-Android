package com.zfdang.zsmth_android.newsmth;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.RectF;
import android.media.ExifInterface;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;
import com.franmontiel.persistentcookiejar.ClearableCookieJar;
import com.franmontiel.persistentcookiejar.PersistentCookieJar;
import com.franmontiel.persistentcookiejar.cache.SetCookieCache;
import com.franmontiel.persistentcookiejar.persistence.SharedPrefsCookiePersistor;
import com.zfdang.SMTHApplication;
import com.zfdang.zsmth_android.helpers.StringUtils;
import com.zfdang.zsmth_android.models.Board;
import com.zfdang.zsmth_android.models.BoardListContent;
import com.zfdang.zsmth_android.models.BoardSection;
import com.zfdang.zsmth_android.models.Mail;
import com.zfdang.zsmth_android.models.MailListContent;
import com.zfdang.zsmth_android.models.Post;
import com.zfdang.zsmth_android.models.Topic;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;
import rx.Observable;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

/**
 * Created by zfdang on 2016-3-16.
 */
public class SMTHHelper {

    static final private String TAG = "SMTHHelper";
    public static final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_11_6) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/53.0.2785.101 Safari/537.36";

    public ClearableCookieJar mCookieJar;
    public OkHttpClient mHttpClient;

    // WWW service of SMTH, but actually most of services are actually from nForum
    private final String SMTH_WWW_URL = "http://www.newsmth.net";
    private Retrofit wRetrofit = null;
    public SMTHWWWService wService = null;
    static private final String SMTH_WWW_ENCODING = "GB2312";

    // Mobile service of SMTH
    // this interface is not used any longer
//    private final String SMTH_MOBILE_URL = "http://m.newsmth.net";
//    private Retrofit mRetrofit = null;
//    public SMTHMobileService mService = null;

    // All boards cache file
    public static int BOARD_TYPE_FAVORITE = 1;
    public static int BOARD_TYPE_ALL = 2;
    static private final String ALL_BOARD_CACHE_FILE = "SMTH_ALLBD_CACHE_KRYO";
    static private final String FAVORITE_BOARD_CACHE_PREFIX = "SMTH_FAVBD_CACHE_KYRO";

    // singleton
    private static SMTHHelper instance = null;

    public static SMTHHelper getInstance() {
        if(instance == null) {
            instance = new SMTHHelper(SMTHApplication.getAppContext());
        }
        return instance;
    }

    // response from WWW is GB2312, we need to conver it to UTF-8
    // http://www.newsmth.net/mainpage.html
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
    protected SMTHHelper(final Context context) {

        // set your desired log level
        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        logging.setLevel(HttpLoggingInterceptor.Level.NONE);

        // https://github.com/franmontiel/PersistentCookieJar
        // A persistent CookieJar implementation for OkHttp 3 based on SharedPreferences.
        mCookieJar = new PersistentCookieJar(new SetCookieCache(), new SharedPrefsCookiePersistor(context));

        //设置缓存路径
        File httpCacheDirectory = new File(SMTHApplication.getAppContext().getCacheDir(), "Responses");
        int cacheSize = 250 * 1024 * 1024; // 250 MiB
        Cache cache = new Cache(httpCacheDirectory, cacheSize);

        mHttpClient = new OkHttpClient().newBuilder()
                .addInterceptor(logging)
                .addInterceptor(new Interceptor() {
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Request request = chain.request()
                                .newBuilder()
                                .header("User-Agent", USER_AGENT)
                                .build();
                        return chain.proceed(request);
                    }
                })
                .addNetworkInterceptor(new Interceptor() {
                    // for error response, do not cache its content
                    @Override
                    public Response intercept(Chain chain) throws IOException {
                        Response originalResponse = chain.proceed(chain.request());
                        if( originalResponse.isSuccessful() ) {
                            return originalResponse;
                        } else {
                            return originalResponse.newBuilder()
                                    .header("Cache-Control", "no-cache")
                                    .build();
                        }
                    }
                })
                .cookieJar(mCookieJar)
                .cache(cache)
                .readTimeout(15, TimeUnit.SECONDS)
                .connectTimeout(10, TimeUnit.SECONDS)
                .build();

//        mRetrofit = new Retrofit.Builder()
//                .baseUrl(SMTH_MOBILE_URL)
//                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
//                .addConverterFactory(ScalarsConverterFactory.create())
//                .client(mHttpClient)
//                .build();
//        mService = mRetrofit.create(SMTHMobileService.class);

        wRetrofit = new Retrofit.Builder()
                .baseUrl(SMTH_WWW_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .client(mHttpClient)
                .build();
        wService = wRetrofit.create(SMTHWWWService.class);
    }

    // query active user status
    // since wService.queryActiveUserStatus does not return correct faceurl, try to query user information again
    public static Observable<UserStatus> queryActiveUserStatus() {
        final SMTHHelper helper = SMTHHelper.getInstance();
        return helper.wService.queryActiveUserStatus()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .map(new Func1<UserStatus, UserStatus>() {
                    @Override
                    public UserStatus call(UserStatus userStatus) {
                        String userid = userStatus.getId();
                        if(userid != null && !userid.equals("guest")) {
                            // get correct faceURL
                            List<UserInfo> users = helper.wService.queryUserInformation(userid).toList().toBlocking().single();
                            if(users.size() == 1) {
                                UserInfo user = users.get(0);
                                userStatus.setFace_url(user.getFace_url());
                            }
                        }
                        return userStatus;
                    }
                });
    }

    private static Bitmap loadResizedBitmapFromFile(final String filename, final int targetWidth, final int targetHeight, boolean bCompress) {
        try {
            BitmapFactory.Options option = null;
            Bitmap bitmap = null;

            // o.inPurgeable = true;
            bitmap = BitmapFactory.decodeFile(filename, option);
            Log.d(TAG, "loadResizedBitmapFromFile: " + String.format("Pre-sized bitmap size: (%dx%d).", bitmap.getWidth(), bitmap.getHeight()));

            if(bCompress) {
                // create bitmap which matches exactly within the target size
                // calc exact destination size
                // http://developer.android.com/reference/android/graphics/Matrix.ScaleToFit.html
                Matrix m = new Matrix();
                RectF inRect = new RectF(0, 0, bitmap.getWidth(), bitmap.getHeight());
                RectF outRect = new RectF(0, 0, targetWidth, targetHeight);
                m.setRectToRect(inRect, outRect, Matrix.ScaleToFit.CENTER);
                float[] values = new float[9];
                m.getValues(values);

                Log.d(TAG, "loadResizedBitmapFromFile: " + String.format("Zoom: (%fx%f).", values[0], values[4]));
                if (values[0] < 1.0 || values[4] < 1.0) {
                    bitmap = Bitmap.createScaledBitmap(bitmap, (int) (bitmap.getWidth() * values[0]),
                            (int) (bitmap.getHeight() * values[4]), true);
                    Log.d(TAG, "loadResizedBitmapFromFile: " + "reduce size");
                }
            }

            Log.d(TAG, "loadResizedBitmapFromFile: " + String.format("Final bitmap size: (%dx%d).", bitmap.getWidth(), bitmap.getHeight()));
            return bitmap;
        } catch (final OutOfMemoryError e) {
            return null;
        }
    }

    /**
     * Returns the contents of the file in a byte array
     * @param file File this method should read
     * @return byte[] Returns a byte[] array of the contents of the file
     */
    private static byte[] getBytesFromFile(File file){
        byte[] bytes = null;
        try{
            InputStream is = new FileInputStream(file);

            // Get the size of the file
            long length = file.length();
            if (length > Integer.MAX_VALUE) {
                Log.e(TAG, "getBytesFromFile: " + "File is too large to process");
                return bytes;
            }

            // Create the byte array to hold the data
            bytes = new byte[(int)length];

            // Read in the bytes
            int offset = 0;
            int numRead = 0;
            while ( (offset < bytes.length)
                    &&
                    ( (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) ) {
                offset += numRead;
            }

            // Ensure all the bytes have been read in
            if (offset < bytes.length) {
                throw new IOException("Could not completely read file " + file.getName());
            }
            is.close();
        } catch (IOException e) {
            Log.e(TAG, "getBytesFromFile: " + Log.getStackTraceString(e) );
        }

        return bytes;
    }

    public static String saveBitmapToFile(final Bitmap bitmap, final String filename) {
        try {
            if (TextUtils.equals(Environment.getExternalStorageState(), Environment.MEDIA_MOUNTED)) {
                String path = Environment.getExternalStorageDirectory().getPath() + "/zSMTH/uploaded";
                File dir = new File(path);
                if (!dir.exists()) {
                    dir.mkdirs();
                }

                File outFile = new File(dir, new File(filename).getName());

                FileOutputStream fstream = new FileOutputStream(outFile);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 85, fstream);
                fstream.flush();
                fstream.close();

                Log.d(TAG, "saveBitmapToFile: " + outFile.getAbsolutePath());
                return outFile.getAbsolutePath();
            }
        } catch (Exception e) {
            Log.e(TAG, "saveBitmapToFile: ", e);
        }
        return null;
    }

    public static void copyExif(String oldPath, String newPath)
    {
        try {
            ExifInterface oldExif = new ExifInterface(oldPath);
            String[] attributes = new String[]
                    {
                            ExifInterface.TAG_APERTURE,
                            ExifInterface.TAG_DATETIME,
                            ExifInterface.TAG_DATETIME_DIGITIZED,
                            ExifInterface.TAG_EXPOSURE_TIME,
                            ExifInterface.TAG_FLASH,
                            ExifInterface.TAG_FOCAL_LENGTH,
                            ExifInterface.TAG_GPS_ALTITUDE,
                            ExifInterface.TAG_GPS_ALTITUDE_REF,
                            ExifInterface.TAG_GPS_DATESTAMP,
                            ExifInterface.TAG_GPS_LATITUDE,
                            ExifInterface.TAG_GPS_LATITUDE_REF,
                            ExifInterface.TAG_GPS_LONGITUDE,
                            ExifInterface.TAG_GPS_LONGITUDE_REF,
                            ExifInterface.TAG_GPS_PROCESSING_METHOD,
                            ExifInterface.TAG_GPS_TIMESTAMP,
                            ExifInterface.TAG_IMAGE_LENGTH,
                            ExifInterface.TAG_IMAGE_WIDTH,
                            ExifInterface.TAG_ISO,
                            ExifInterface.TAG_MAKE,
                            ExifInterface.TAG_MODEL,
                            ExifInterface.TAG_ORIENTATION,
                            ExifInterface.TAG_SUBSEC_TIME,
                            ExifInterface.TAG_SUBSEC_TIME_DIG,
                            ExifInterface.TAG_SUBSEC_TIME_ORIG,
                            ExifInterface.TAG_WHITE_BALANCE
                    };

            ExifInterface newExif = new ExifInterface(newPath);
            for (int i = 0; i < attributes.length; i++)
            {
                String value = oldExif.getAttribute(attributes[i]);
                if (value != null)
                    newExif.setAttribute(attributes[i], value);
            }
            newExif.saveAttributes();
        } catch (IOException e) {
            Log.e(TAG, "copyExif: ", e);
        }
    }

    public static byte[] getBitmapBytesWithResize(final String filename, boolean bCompress){
        final SMTHHelper helper = SMTHHelper.getInstance();
        Log.d(TAG, "getBitmapBytesWithResize: " + filename);

        if(filename.toLowerCase().endsWith(".gif")) {
            // gif, don't resize
            File infile = new File(filename);
            byte[] byteArray = getBytesFromFile(infile);
            return byteArray;
        } else {
            // static image, resize it
            Bitmap theBitmap = loadResizedBitmapFromFile(filename, 1200, 1200, bCompress);

            // save bitmap to temp file
            String newfilename = saveBitmapToFile(theBitmap, filename);

            // copy exif information from old file to new file
            copyExif(filename, newfilename);

            // read data
            File infile = new File(newfilename);
            byte[] byteArray = getBytesFromFile(infile);
            return byteArray;
        }
    }


    public static Observable<AjaxResponse> publishPost(String boardEngName,
                                           String subject,
                                           String content,
                                           String signature,
                                           String replyPostID) {
        SMTHHelper helper = SMTHHelper.getInstance();
        return helper.wService.publishPost(boardEngName, subject, content, signature, replyPostID)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());
    }

    public static Observable<AjaxResponse> editPost(String boardEngName,
                                                    String postID,
                                                    String subject,
                                                    String content) {
        SMTHHelper helper = SMTHHelper.getInstance();
        return helper.wService.editPost(boardEngName, postID, subject, content)
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());
    }


    public static Observable<AjaxResponse> sendMail(String userid,
                                                       String title,
                                                       String content
                                                       ) {
        SMTHHelper helper = SMTHHelper.getInstance();
        return helper.wService.sendMail("NULL", userid, title, content, "0", "on", "")
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io());
    }


    public static Post ParseMailContentFromWWW(String content) {
        Post post = new Post();

        List<String> likes = new ArrayList<>();
        Document doc = Jsoup.parse(content);
        post.setLikesAndPostContent(likes, doc);

        return post;
    }

    public static List<Post> ParsePostListFromWWW(String content, Topic topic) {
        final String TAG = "ParsePostListFromWWW";
        List<Post> results = new ArrayList<>();

        Document doc = Jsoup.parse(content);

        // find total posts for this topic, and total pages
        Elements lis = doc.select("li.page-pre");
        if(lis.size() > 0) {
            Element li = lis.first();
            // 贴数:152 分页:
//            Log.d(TAG, li.text());

            Pattern pattern = Pattern.compile("(\\d+)", Pattern.DOTALL);
            Matcher matcher = pattern.matcher(li.text());
            if (matcher.find()) {
                String totalPostString = matcher.group(0);
                topic.setTotalPostNoFromString(totalPostString);
//                Log.d(TAG, totalPostString);
            }
        }

        // find all posts
        Elements tables = doc.select("table.article");
        for (Element table: tables) {
            Post post = new Post();

            // find author for this post
            // <span class="a-u-name"><a href="/nForum/user/query/CZB">CZB</a></span>
            Elements authors = table.select("span.a-u-name");
            if(authors.size() > 0){
                Element author = authors.get(0);
                String authorName = author.text();
                post.setAuthor(authorName);
            }

            // find post id for this post
            // <samp class="ico-pos-reply"></samp><a href="/nForum/article/WorkLife/post/1113865" class="a-post">回复</a></li>
            Elements links = table.select("li a.a-post");
            if(links.size() > 0){
                Element link = links.first();
                String postID = StringUtils.getLastStringSegment(link.attr("href"));
                post.setPostID(postID);
            }

            // find post position
            // <span class="a-pos">第1楼</span>
            Elements positions = table.select("span.a-pos");
            if(positions.size() > 0){
                Element position = positions.first();
                post.setPosition(position.text());
            }


            // find & parse post content
            Elements contents = table.select("td.a-content");
            if(contents.size() == 1) {
                ParsePostContentFromWWW(contents.get(0), post);
            }
//            Log.d(TAG, post.toString());
            results.add(post);
        }

        if(results.size() == 0) {
            // there might be some problems with the response
//            <div class="error">
//            <h5>产生错误的可能原因：</h5>
//            <ul>
//            <li>
//            <samp class="ico-pos-dot"></samp>指定的文章不存在或链接错误</li>
//            </ul>
//            </div>
            Elements divs = doc.select("div.error");
            if(divs.size() > 0) {
                Element div = divs.first();

                topic.setTotalPostNoFromString("1");

                Post post = new Post();
                post.setAuthor("错误信息");
                post.setRawContent(div.toString());
                results.add(post);
            }



        }

        return results;
    }


    // called by ParsePostListFromWWW
    // this method will call ParseLikeElementInPostContent & ParsePostBodyFromWWW
    // sample response: assets/post_content_from_www.html
    public static void ParsePostContentFromWWW(Element content, Post post) {
        // 1. find, parse and remove likes node first
        // <div class="likes">
        Elements nodes = content.select("div.likes");
        List<String> likes = null;
        if(nodes.size() == 1) {
            Element node = nodes.first();
            likes = ParseLikeElementInPostContent(node);
        }

        // 2. find post content, the first <p> node in the td.a-content
        // <button class="button add_like"
        nodes = content.getElementsByTag("p");
        if(nodes.size() >= 1) {
            Element node = nodes.first();
            // 2. set post content
            post.setLikesAndPostContent(likes, node);
        }
    }

    // parse like list in post content
    public static List<String> ParseLikeElementInPostContent(Element like) {
        List<String> likes = new ArrayList<>();

        // <div class="like_name">有36位用户评价了这篇文章：</div>
        Elements nodes = like.select("div.like_name");
        if(nodes.size() == 1) {
            Element node = nodes.first();
            likes.add(node.text());
        }

        // <li><span class="like_score_0">[&nbsp;&nbsp;]</span><span class="like_user">fly891198061:</span>
        // <span class="like_msg">无法忍受，我不会变节，先斗智，不行就自杀！来个痛快的~！</span>
        // <span class="like_time">(2016-03-27 15:04)</span></li>
        nodes = like.select("li");
        for(Element n: nodes) {
            likes.add(n.text());
        }

        return likes;
    }

    public static Topic ParseTopicFromElement(Element ele, String type) {
        if("top10".equals(type) ||  "sectionhot".equals(type)) {
            // two <A herf> nodes

            // normal hot topic
            // <li><a href="/nForum/article/OurEstate/1685281" title="lj让我走垫资(114)">lj让我走垫资&nbsp;(114)</a></li>

            // special hot topic -- 近期热帖: 1. board信息，没有reply_count
            // <li>
            // <div><a href="/nForum/board/Picture"><span class="board">[贴图]</span></a><a href="/nForum/article/ShiDa/59833" title=" 南都副总编及编辑被处分开除"><span class="title"> 南都副总编及编辑被处分开除</span></a></div>
            // </li>

            Elements as = ele.select("a[href]");
            if(as.size() == 2) {
                Element a1 = as.get(0);
                Element a2 = as.get(1);

                String boardChsName = a1.text().replace("]", "").replace("[", "");
                String boardEngName = StringUtils.getLastStringSegment(a1.attr("href"));

                String title = a2.attr("title");
                String topicID = StringUtils.getLastStringSegment(a2.attr("href"));

                Topic topic = new Topic();
                String reply_count = StringUtils.getReplyCountInParentheses(title);
                if(reply_count.length() > 0) {
                    title = title.substring(0, title.length() - reply_count.length() - 2);
                    topic.setTotalPostNoFromString(reply_count);
                }

                topic.setBoardEngName(boardEngName);
                topic.setBoardChsName(boardChsName);
                topic.setTopicID(topicID);
                topic.setTitle(title);

//                Log.d(TAG, topic.toString());
                return topic;
            }
        } else if("pictures".equals(type)) {
            // three <A herf> nodes

            // <li>
            // <a href="/nForum/article/SchoolEstate/430675"><img src="http://images.newsmth.net/nForum/img/hotpic/SchoolEstate_430675.jpg" title="点击查看原帖" /></a>
            // <br /><a class="board" href="/nForum/board/SchoolEstate">[学区房]</a>
            // <br /><a class="title" href="/nForum/article/SchoolEstate/430675" title="这个小学排名还算靠谱吧， AO爸爸排的。。。">这个小学排名还算靠谱吧， AO爸爸排的。。。</a>
            // </li>
            Elements as = ele.select("a[href]");
            if(as.size() == 3) {
                Element a1 = as.get(1);
                Element a2 = as.get(2);

                String boardChsName = a1.text().replace("]", "").replace("[", "");
                String boardEngName = StringUtils.getLastStringSegment(a1.attr("href"));

                String title = a2.attr("title");
                String topicID = StringUtils.getLastStringSegment(a2.attr("href"));


                Topic topic = new Topic();
                topic.setBoardEngName(boardEngName);
                topic.setBoardChsName(boardChsName);
                topic.setTopicID(topicID);
                topic.setTitle(title);

//                Log.d(TAG, topic.toString());
                return topic;
            }

        }
        return null;
    }


    // parse guidance page, to find all hot topics
    public static List<Topic> ParseHotTopicsFromWWW(String content) {
        List<Topic> results = new ArrayList<>();
        if (content == null || content.length() == 0) {
            return results;
        }

        Topic topic = null;
        Document doc = Jsoup.parse(content);

        // find top10
        // <div id="top10">
        Elements top10s = doc.select("div#top10");
        if(top10s.size() == 1) {
            // add separator
            topic = new Topic("本日十大热门话题");
            results.add(topic);

            // parse hot hopic
            Element top10 = top10s.first();
            Elements lis = top10.getElementsByTag("li");

            for(Element li: lis) {
                topic = ParseTopicFromElement(li, "top10");
                if(topic != null) {
//                    Log.d(TAG, topic.toString());
                    results.add(topic);
                }
            }
        }


        // find hotspot
        // <div id="hotspot" class="block">
        // skip this part, it's tedious
//        Elements hotspots = doc.select("div#hotspot div.topics");
//        if(hotspots.size() == 1) {
//            // add separator
//            topic = new Topic("近期热帖");
//            results.add(topic);
//
//            // parse hot hopic
//            Element hotspot = hotspots.first();
//            Elements lis = hotspot.getElementsByTag("li");
//
//            for(Element li: lis) {
//                topic = ParseTopicFromElement(li, "hotspot");
//                if(topic != null) {
////                    Log.d(TAG, topic.toString());
//                    results.add(topic);
//                }
//            }
//        }

        // find hot picture
        // <div id="pictures" class="block">
        Elements pictures = doc.select("div#pictures");
        for(Element section: pictures) {
            // add separator
            Elements sectionNames = section.getElementsByTag("h3");
            if(sectionNames.size() == 1) {
                Element sectionName = sectionNames.first();
                topic = new Topic(sectionName.text());
                results.add(topic);
            }

            Elements lis = section.select("div li");
            for (Element li: lis) {
//                Log.d(TAG, li.toString());
                topic = ParseTopicFromElement(li, "pictures");
                if(topic != null) {
//                    Log.d(TAG, topic.toString());
                    results.add(topic);
                }
            }
        }

        // find hot topics from each section
        // <div id="hotspot" class="block">
        Elements sections = doc.select("div.b_section");
        for(Element section: sections) {
            // add separator
            Elements sectionNames = section.getElementsByTag("h3");
            if(sectionNames.size() == 1) {
                Element sectionName = sectionNames.first();
                String name = sectionName.text();
                if(name == null || name.equals("系统与祝福")){
                    continue;
                }
                topic = new Topic(name);
                results.add(topic);
            }

            Elements lis = section.select("div.topics li");
            for (Element li: lis) {
//                Log.d(TAG, li.toString());
                topic = ParseTopicFromElement(li, "sectionhot");
                if(topic != null) {
//                    Log.d(TAG, topic.toString());
                    results.add(topic);
                }
            }
        }

        return results;
    }


    // parse board topics from WWW
    public static List<Topic> ParseBoardTopicsFromWWW(String content) {
        List<Topic> results = new ArrayList<>();
        if (content == null) {
            return results;
        }

        Document doc = Jsoup.parse(content);

        // <li class="page-select"><a title="当前页">2</a></li>
        String currentPage = null;
        Elements lis = doc.select("li.page-select");
        if(lis.size() > 0) {
            Element li = lis.first();
            currentPage = li.text();
//            Log.d(TAG, "ParseBoardTopicsFromWWW: " + currentPage);
        }

//        <tr class="top">
//        <td class="title_8">
//        <a target="_blank" href="/nForum/article/FamilyLife/1757972219" title="在新窗口打开此主题">
//        <samp class="tag ico-pos-article-light"></samp>
//        </a>
//        </td>
//        <td class="title_9"><a href="/nForum/article/FamilyLife/1757972219">2岁女孩找妈妈 其父母终于被找到</a>
//        <samp class="tag-att ico-pos-article-attach"></samp><span class="threads-tab">[<a href="/nForum/article/FamilyLife/1757972219?p=2">2</a>]</span></td>
//        <td class="title_10">2016-05-05</td>
//        <td class="title_12">|&ensp;<a href="/nForum/user/query/Muscle" class="c63f">Muscle</a></td>
//        <td class="title_11 middle"></td>
//        <td class="title_11 middle"></td>
//        <td class="title_11 middle">14</td>
//        <td class="title_10"><a href="/nForum/article/FamilyLife/1757972219?p=2#a14" title="跳转至最后回复">21:38:58&emsp;</a></td>
//        <td class="title_12">|&ensp;<a href="/nForum/user/query/kxxx" class="c09f">kxxx</a></td>
//        </tr>

        // get all trs
        Elements trs = doc.select("table.board-list tbody tr");
        for (Element tr: trs) {
//            Log.d(TAG, "ParseBoardTopicsFromWWW: " + tr.toString());
            Topic topic = new Topic();

            String trClass = tr.attr("class");
            if(TextUtils.equals(trClass, "top")) {
                // is sticky
                topic.isSticky = true;
            }

            Elements tds = tr.getElementsByTag("td");
            for(Element td: tds) {
                String tdClass = td.attr("class");
//                Log.d(TAG, "ParseBoardTopicsFromWWW: td.class = " + tdClass);

                if(TextUtils.equals(tdClass, "title_9")){
                    // <td class="title_9"><a href="/nForum/article/FamilyLife/1757972219">2岁女孩找妈妈 其父母终于被找到</a>
                    Elements as = td.getElementsByTag("a");
                    if(as.size() > 0) {
                        Element a = as.first();
                        topic.setTitle(a.text());

                        String topicURL = a.attr("href");
                        topic.setTopicID(StringUtils.getLastStringSegment(topicURL));
                    }
                    // <samp class="tag-att ico-pos-article-attach"></samp>
                    // find attachment flag
                    Elements samps = td.getElementsByTag("samp");
                    if(samps != null && samps.size() > 0) {
                        topic.setHasAttach(true);
                    }

                } else if (TextUtils.equals(tdClass, "title_10")) {
                    // <td class="title_10">2016-05-05</td>
                    // <td class="title_10"><a href="/nForum/article/FamilyLife/1757972219?p=2#a14" title="跳转至最后回复">21:38:58&emsp;</a></td>
                    String publishDate = topic.getPublishDate();
                    if(publishDate == null || publishDate.length() == 0 ) {
                        topic.setPublishDate(td.text());
                    } else {
                        topic.setReplyDate(td.text());
                    }

                } else if (TextUtils.equals(tdClass, "title_12")) {
                    // <td class="title_12">|&ensp;<a href="/nForum/user/query/Muscle" class="c63f">Muscle</a></td>
                    // <td class="title_12">|&ensp;<a href="/nForum/user/query/kxxx" class="c09f">kxxx</a></td>
                    String author = topic.getAuthor();
                    String value = td.text().replace("|", "").trim();
                    if(author == null || author.length() == 0 ) {
                        topic.setAuthor(value);
                    } else {
                        topic.setReplier(value);
                    }

                } else if (TextUtils.equals(tdClass, "title_11 middle")) {
                    // <td class="title_11 middle">评分</td>
                    // <td class="title_11 middle">like</td>
                    // <td class="title_11 middle">回复: 14</td>
                    String score = topic.getScore();
                    String likes = topic.getLikes();
                    String value = td.text();
                    if(score == null) {
                        topic.setScore(value);
                    } else if (likes == null) {
                        topic.setLikes(value);
                    } else {
                        topic.setReplyCounts(value);
                    }
                }
            }

            // Log.d("ParseBoardTopics", topic.toString());
            if(topic.getTitle() != null && topic.getTitle().length() > 0) {
                results.add(topic);
            }
        }

        return results;
    }


    // parse topics from nForum search results
//    <tr>
//    <td class="title_8">1.</td>
//    <td class="title_14">
//    <a target="_blank" href="/nForum/article/PocketLife/2217534" title="在新窗口打开此主题">
//    <samp class="tag ico-pos-article-normal"></samp>
//    </a>
//    </td>
//    <td class="title_9"><a href="/nForum/article/PocketLife/2217534">更改了一下zSMTH&#45;Android的颜色搭配</a></td>
//    <td class="title_10">08:47:21&emsp;</td>
//    <td class="title_12">|&ensp;<a href="/nForum/user/query/mozilla" class="c63f">mozilla</a></td>
//    <td class="title_11 middle">9</td>
//    <td class="title_10"><a href="/nForum/article/PocketLife/2217534?p=1#a9" title="跳转至最后回复">11:04:03&emsp;</a></td>
//    <td class="title_12">|&ensp;<a href="/nForum/user/query/mozilla" class="c09f">rasper</a></td>
//    </tr>
    public static List<Topic> ParseSearchResultFromWWW(String content) {
        final String TAG = "ParseSearchResult";

        List<Topic> results = new ArrayList<>();
        if (content == null) {
            return results;
        }
        // Log.d(TAG, content);

        // parse topics using Jsoup
        Document doc = Jsoup.parse(content);

        // get all lis
        Elements divs = doc.select("div.b-content");
        if(divs.size() == 0) {
            Log.d(TAG, "ParseSearchResultFromWWW: " + "Did not find div.b-content");
            return results;
        }
        Element div = divs.first();

        Elements trs = div.getElementsByTag("tr");
        for (Element tr: trs) {
            // Log.d(TAG, "ParseSearchResultFromWWW: " + tr.toString());
            Elements tds = tr.getElementsByTag("td");
            if(tds.size() == 0) {
                continue;
            }

            Topic topic = new Topic();
            String title = "";
            String author = "";
            String replier = "";
            String publishDate = "";
            String replyDate = "";
            String topicID = "";

            for(Element td: tds) {
                if(TextUtils.equals(td.attr("class"), "title_9")){
                    title = td.text();
                    Elements As = td.getElementsByTag("A");
                    if(As.size() > 0) {
                        Element A = As.first();
                        topicID = StringUtils.getLastStringSegment(A.attr("href"));
                    }
                } else if(TextUtils.equals(td.attr("class"), "title_10")){
                    if(publishDate.length() == 0) {
                        publishDate = td.text();
                    } else {
                        replyDate = td.text();
                    }
                } else if(TextUtils.equals(td.attr("class"), "title_12")){
                    String person = td.text().replace("|", "").trim();
                    if(author.length() == 0) {
                        author = person;
                    } else {
                        replier = person;
                    }
                }
            }
            topic.setAuthor(author);
            topic.setTopicID(topicID);
            topic.setTitle(title);
            topic.setReplier(replier);
            topic.setPublishDate(publishDate);
            topic.setReplyDate(replyDate);

            // Log.d(TAG, "ParseSearchResultFromWWW: " + topic.toString());
            if(topic.getTitle() != null && topic.getTitle().length() > 0) {
                results.add(topic);
            }
        }

        return results;
    }


    public static List<Board> ParseFavoriteBoardsFromWWW(String content) {
        List<Board> boards = new ArrayList<>();

//        o.f(1,'favFolder1 ',0,'');
//        o.o(false,1,896,22556,'[站务]','Advice','水木发展','SYSOP',7026,895,4);
//        o.o(false,1,619,2332235,'[生活]','CouponsLife','辣妈羊毛党','hmilytt XZCL',897207,618,1601);
//        o.o(false,1,179,808676665,'[数码]','DSLR','数码单反','jerryxiao',153110,178,57);
//        o.o(true,1,1368,0,'[数码]','SmartLife','智能生活','[目录]',0,1367,0);

        final String error_msg = "您还没有登录，或者长时间没有动作，请您重新登录";
        if(content.contains(error_msg)){
            Board board = new Board(Board.Invalid_Folder_ID, error_msg + "\n" + "登录后，请在右上角菜单里'刷新'收藏夹");
            boards.add(board);

            return boards;
        }

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

            Board board = null;
            if(TextUtils.equals(moderator, "[目录]")) {
//              o.o(true,1,1368,0,'[数码]','SmartLife','智能生活','[目录]',0,1367,0);
                board = new Board(boardID, chsName + "[二级目录]");
            } else {
                if (moderator.length() > 25) {
                    moderator = moderator.substring(0, 21) + "...";
                }
                board = new Board(boardID, chsName, engName);
                board.setModerator(moderator);
                board.setCategoryName(category);
            }
            boards.add(board);
        }

        return boards;
    }

    // sample input: mailbox_response.html, refer_at_posts.html
    public static List<Mail> ParseMailsFromWWW(String content) {
        List<Mail> mails = new ArrayList<>();

        Document doc = Jsoup.parse(content);

        // <div class="error"><h5>产生错误的可能原因：</h5><ul><li><samp class="ico-pos-dot"></samp>请勿频繁登录</li></ul></div>
        Elements errors = doc.select("div.error");
        if(errors.size() > 0 ){
            Element error = errors.first();
            Mail mail = new Mail(error.text());
            mails.add(mail);
            return mails;
        }

        // <li class="page-pre">邮件总数:<i>177</i>&emsp;分页:</li>
        // <li class="page-pre">文章总数:<i>17</i>&emsp;分页:</li>
        Elements is = doc.select("div.page li.page-pre i");
        if(is.size() > 0) {
            Element i = is.first();
            String totalMails = i.text();
            // Log.d(TAG, "ParseMailsFromWWW: " + totalMails);
            MailListContent.setTotalMails(Integer.parseInt(totalMails));
        }

        // <li class="page-select"><a title="当前页">1</a></li>
        Elements lis = doc.select("div.page li.page-select");
        // Log.d(TAG, "ParseMailsFromWWW: " + lis.toString());
        if(lis.size() > 0) {
            // find
            Element li = lis.first();
            String page = li.text();
            Mail mail = new Mail(String.format("第%s页", page));
            mails.add(mail);
        }

        Elements trs = doc.select("table.m-table tr");
//        <tr class="no-read">
//        <td class="title_1">
//        <input type="checkbox" name="m_175" class="mail-item" />
//        </td>
//        <td class="title_2"><a href="/nForum/user/query/mozilla">mozilla</a></td>
//        <td class="title_3"><a href="/nForum/mail/inbox/175.json" class="mail-detail">Re: 求助，dish的机顶盒到货了，锅怎么办？？？&#40;转寄&#41;</a></td>
//        <td class="title_4">2016-04-27 16:38:54</td>
//        </tr>
        for (Element tr: trs) {
            Mail mail = new Mail();

            if(TextUtils.equals(tr.attr("class"), "no-read")) {
                mail.isNew = true;
            }

            Elements tds = tr.getElementsByTag("td");
            for (Element td: tds) {
                if (TextUtils.equals(td.attr("class"), "title_2")) {
                    // <td class="title_2"><a href="/nForum/user/query/Wunderman">Wunderman</a></td>
                    // <td class="title_2"><a href="/nForum/board/PocketLife">PocketLife</a></td>
                    if(mail.author == null || mail.author.length() == 0) {
                        mail.author = td.text();
                    } else {
                        mail.fromBoard = td.text();
                    }
                } else if (TextUtils.equals(td.attr("class"), "title_3")) {
                    // <td class="title_3"><a href="/nForum/article/PocketLife/ajax_single/2228708.json" class="m-single" _index="16">Re: zSMTH 1.0.0版发布</a></td>
                    mail.title = td.text();
                    Elements as = td.getElementsByTag("a");
                    if(as.size() > 0) {
                        Element a = as.first();
                        mail.url = a.attr("href");
                        mail.referIndex = a.attr("_index");
                    }
                } else if (TextUtils.equals(td.attr("class"), "title_4")) {
                    // <td class="title_4">2016-05-06 04:13:55</td>
                    mail.date = td.text();
                }

            }

            if(mail.author != null && mail.author.length() > 0 && !TextUtils.equals(mail.author, "作者")) {
                // only valid mail will be added
                // referred post have table head, so we make sure author != "作者"
                mails.add(mail);
            }
        }

        if(mails.size() == 0) {
            Mail mail = new Mail(".无信件.");
            mails.add(mail);
        }

        return mails;
    }

    // sample response: assets/deletion_response.html
    public static String parseDeleteResponse(String response) {
        String result = "";
        Document doc = Jsoup.parse(response);
        Elements bodies = doc.getElementsByTag("body");

        if(bodies != null && bodies.size() > 0) {
            Element body = bodies.first();

            Elements divs = body.select("div.nav");
            for (Element div: divs) {
                div.remove();
            }

            Elements as = body.getElementsByTag("a");
            for(Element a : as) {
                a.remove();
            }

            result = body.text();
        }

        result = result.replaceAll("用户名：", "");
        result = result.replaceAll("密　码：", "");
        return result;
    }

    // sample response: assets/deletion_response.html
    public static String parseRepostResponse(String response) {
        if(response == null) {
            return "错误的返回结果";
        }

        if(response.contains("操作成功: 转贴成功！")){
            return "操作成功: 转贴成功！";
        }

        Document doc = Jsoup.parse(response);
        Elements errors = doc.select("table.error");
        if(errors != null && errors.size() > 0) {
            Element error = errors.first();
            return error.text();
        }

        return "未识别的返回";
    }


    /*
    * All Boards related methods
    * Starts here
     */
    public static String getCacheFile(int type, String folder) {
        if(type == BOARD_TYPE_ALL) {
            return ALL_BOARD_CACHE_FILE;
        } else if (type == BOARD_TYPE_FAVORITE) {
            if(folder == null || folder.length() == 0){
                folder = "ROOT";
            }
            return String.format("%s-%s", FAVORITE_BOARD_CACHE_PREFIX, folder);
        }
        return null;
    }

    public static List<Board> LoadBoardListFromCache(int type, String folder){
        String filename = getCacheFile(type, folder);
        List<Board> boards = new ArrayList<>();
        try {
            Kryo kryo = new Kryo();
            Input input = new Input(SMTHApplication.getAppContext().openFileInput(filename));
            boards = kryo.readObject(input, ArrayList.class);
            input.close();
            Log.d("LoadBoardListFromCache", String.format("%d boards loaded from cache file %s", boards.size(), filename));
        } catch (Exception e) {
            Log.d("LoadBoardListFromCache", e.toString());
            Log.d("LoadBoardListFromCache", "failed to load boards from cache file " + filename);
        }
        return boards;
    }

    public static void SaveBoardListToCache(List<Board> boards, int type, String folder){
        String filename = getCacheFile(type, folder);
        try {
            Kryo kryo = new Kryo();
            Output output = new Output(SMTHApplication.getAppContext().openFileOutput(filename, Context.MODE_PRIVATE));
            kryo.writeObject(output, boards);
            output.close();
            Log.d("SaveBoardListToCache", String.format("%d boards saved to cache file %s", boards.size(), filename));
        } catch (Exception e) {
            Log.d("SaveBoardListToCache", e.toString());
            Log.d("SaveBoardListToCache", "failed to save boards to cache file " + filename);
        }
    }

    public static void ClearBoardListCache(int type, String folder) {
        String filename = getCacheFile(type, folder);
        try{
            if(SMTHApplication.getAppContext().deleteFile(filename))
            {
                Log.d("ClearBoardListCache", String.format("delete cache file %s successfully", filename));
                return;
            }
        } catch (Exception e) {
            Log.d("ClearBoardListCache", e.toString());
            Log.d("ClearBoardListCache", "Failed to delete cache file " + filename);
        }
    }

    public static List<Board> LoadFavoriteBoardsByFolderFromWWW(final String path) {
        List<Board> results = SMTHHelper.getInstance().wService.getFavoriteByPath(path)
                .flatMap(new Func1<ResponseBody, Observable<Board>>() {
                    @Override
                    public Observable<Board> call(ResponseBody resp) {
                        try {
                            String response = SMTHHelper.DecodeResponseFromWWW(resp.bytes());
//                            Log.d(TAG, response);
                            List<Board> boards = SMTHHelper.ParseFavoriteBoardsFromWWW(response);
                            return Observable.from(boards);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to load favorite {" + path + "}");
                            Log.e(TAG, Log.getStackTraceString(e));
                            return null;
                        }
                    }
                })
                .toList().toBlocking().single();

        SaveBoardListToCache(results, BOARD_TYPE_FAVORITE, path);

        return results;
    }


    public static List<Board> LoadFavoriteBoardsInGroupFromWWW(final String path) {
        List<Board> results = SMTHHelper.getInstance().wService.getBoardsInGroup(path)
                .flatMap(new Func1<ResponseBody, Observable<Board>>() {
                    @Override
                    public Observable<Board> call(ResponseBody resp) {
                        try {
                            String response = SMTHHelper.DecodeResponseFromWWW(resp.bytes());
//                            Log.d(TAG, response);
                            List<Board> boards = SMTHHelper.ParseFavoriteBoardsFromWWW(response);
                            return Observable.from(boards);
                        } catch (Exception e) {
                            Log.e(TAG, "Failed to load favorite {" + path + "}");
                            Log.e(TAG, Log.getStackTraceString(e));
                            return null;
                        }
                    }
                })
                .toList().toBlocking().single();

        SaveBoardListToCache(results, BOARD_TYPE_FAVORITE, path);

        return results;
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
        SaveBoardListToCache(boards, BOARD_TYPE_ALL, null);

        return boards;
    }

    public static Observable<Board> loadChildBoardsRecursivelyFromWWW(Board board) {
        if(board.isFolder()) {
            BoardSection section = new BoardSection();
            section.sectionURL = board.getFolderID();
            section.sectionName = board.getFolderName();
            section.parentName = board.getCategoryName();

            // load recruisively
            return SMTHHelper.loadBoardsInSectionFromWWW(section)
                    .subscribeOn(Schedulers.io())
                    .observeOn(Schedulers.io())
                    .flatMap(new Func1<Board, Observable<Board>>() {
                        @Override
                        public Observable<Board> call(Board board) {
                            return loadChildBoardsRecursivelyFromWWW(board);
                        }
                    });
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
                            Log.e(TAG, Log.getStackTraceString(e));
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
