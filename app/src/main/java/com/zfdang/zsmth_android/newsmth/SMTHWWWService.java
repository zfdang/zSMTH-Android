package com.zfdang.zsmth_android.newsmth;


import okhttp3.ResponseBody;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by zfdang on 2016-3-16.
 */


public interface SMTHWWWService {
//    @FormUrlEncoded
//    @POST("/bbslogin.php")
//    Observable<ResponseBody> login(@Field("id") String username, @Field("passwd") String password);

    @FormUrlEncoded
    @POST("/bbslogin.php")
    Observable<ResponseBody> loginWithKick(@Field("id") String username, @Field("passwd") String password, @Field("kick_multi") String kickID);

    @GET("/bbsfav.php")
    Observable<ResponseBody> getFavoriteByPath(@Query("select") String path);

    @GET("/nForum/section/{section}?ajax")
    Observable<ResponseBody> getBoardsBySection(@Path("section") String section);

    @GET("/nForum/article/{boardEngName}/{topicID}?ajax")
    Observable<ResponseBody> getPostListByPage(@Path("boardEngName") String boardEngName, @Path("topicID") String topicID, @Query("p") int page);

}
