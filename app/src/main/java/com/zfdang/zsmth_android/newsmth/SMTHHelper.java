package com.zfdang.zsmth_android.newsmth;

import android.text.Html;
import android.util.Log;

import com.zfdang.zsmth_android.models.Topic;

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
            instance = new SMTHHelper();
        }
        return instance;
    }

    public static String DecodeWWWResponse(byte[] bytes) {
        String result = null;
        try {
            result = new String(bytes, SMTH_WWW_ENCODING);
        } catch (UnsupportedEncodingException e) {
            Log.d("DecodeWWWResponse", e.toString());
        }
        return result;
    }

    // can only be called by getInstance
    protected SMTHHelper() {

        HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
        // set your desired log level
        logging.setLevel(HttpLoggingInterceptor.Level.BODY);

        // add logging as last interceptor
        httpClient = new OkHttpClient().newBuilder().addInterceptor(logging).build();

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


    public static List<Topic> ParseHotTopics(String content) {
        List<Topic> results = new ArrayList<Topic>();
        if (content == null) {
            return results;
        }

        Pattern hp = Pattern.compile("<table [^<>]+class=\"HotTable\"[^<>]+>(.*?)</table>", Pattern.DOTALL);
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
}
