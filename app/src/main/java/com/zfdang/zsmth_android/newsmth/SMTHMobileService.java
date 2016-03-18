package com.zfdang.zsmth_android.newsmth;

import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import rx.Observable;

/**
 * Created by zfdang on 2016-3-17.
 */
public interface SMTHMobileService {
    @FormUrlEncoded
    @POST("/user/login")
    Observable<ResponseBody> login(@Field("id") String username, @Field("passwd") String password, @Field("save") String save);

    @GET("/hot/{index}")
    Observable<ResponseBody> hotTopics(@Path("index") String index);

    // http://m.newsmth.net/article/DSLR/808676538
    @GET("/article/{board}/{article_id}")
    Observable<String> article(@Path("board") String board, @Path("article_id") String article_id);
}
