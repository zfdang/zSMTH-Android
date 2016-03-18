package com.zfdang.zsmth_android.newsmth;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by zfdang on 2016-3-17.
 */
public interface SMTHMobileService {
    @GET("/article/{board}/{article_id}")
    Observable<String> article(@Path("board") String board, @Path("article_id") String article_id);

    @GET("/hot/{index}")
    Observable<ResponseBody> hotTopics(@Path("index") String index);
}
