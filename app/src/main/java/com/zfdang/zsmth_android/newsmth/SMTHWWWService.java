package com.zfdang.zsmth_android.newsmth;


import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by zfdang on 2016-3-16.
 */


public interface SMTHWWWService {

    @FormUrlEncoded
    @Headers("X-Requested-With:XMLHttpRequest")
    @POST("/nForum/user/ajax_login.json")
    Observable<AjaxResponse> login(@Field("id") String username, @Field("passwd") String password, @Field("CookieDate") String CookieDate);

    // {"ajax_st":1,"ajax_code":"0005","ajax_msg":"操作成功"}
    @Headers("X-Requested-With:XMLHttpRequest")
    @GET("/nForum/user/ajax_logout.json")
    Observable<AjaxResponse> logout();

    @GET("/bbsfav.php")
    Observable<ResponseBody> getFavoriteByPath(@Query("select") String path);

    @GET("/nForum/section/{section}?ajax")
    Observable<ResponseBody> getBoardsBySection(@Path("section") String section);

    @GET("/nForum/article/{boardEngName}/{topicID}?ajax")
    Observable<ResponseBody> getPostListByPage(@Path("boardEngName") String boardEngName, @Path("topicID") String topicID, @Query("p") int page);

    @Headers("X-Requested-With:XMLHttpRequest")
    @GET("/nForum/mainpage?ajax")
    Observable<ResponseBody> getAllHotTopics();

    // http://www.newsmth.net/nForum/s/article?ajax&t1=ad&au=ad&m=on&a=on&b=WorkLife
    @Headers("X-Requested-With:XMLHttpRequest")
    @GET("/nForum/s/article?ajax")
    Observable<ResponseBody> searchTopicInBoard(@Query("t1") String keyword,
                                                @Query("au") String author,
                                                @Query("m") String elite,
                                                @Query("a") String attachment,
                                                @Query("b") String boardEngName);

    // the header line is important, because newsmth will ignore it without this header
    @Headers("X-Requested-With:XMLHttpRequest")
    @GET("/nForum/user/query/{username}.json")
    Observable<UserInfo> queryUserInformation(@Path("username") String username);


    @Headers({"Content-Type: application/octet-stream", "X-Requested-With:XMLHttpRequest"})
    @POST("/nForum/att/{boardEngName}/ajax_add.json")
    Observable<AjaxResponse> uploadAttachment(@Path("boardEngName") String boardEngName,
                                              @Query("name") String filename, @Body RequestBody fileContent);


    @FormUrlEncoded
    @Headers("X-Requested-With:XMLHttpRequest")
    @POST("/nForum/article/{boardEngName}/ajax_post.json")
    Observable<AjaxResponse> publishPost(@Path("boardEngName") String boardEngName,
                                         @Field("subject") String subject,
                                         @Field("content") String content,
                                         @Field("signature") String signature,
                                         @Field("id") String id);

    @Headers("X-Requested-With:XMLHttpRequest")
    @GET("/nForum/user/ajax_session.json")
    Observable<UserStatus> queryActiveUserStatus();


    @FormUrlEncoded
    @Headers("X-Requested-With:XMLHttpRequest")
    @POST("/nForum/fav/op/{favid}.json")
    Observable<AjaxResponse> manageFavoriteBoard(@Path("favid") String favid,
                                                 @Field("ac") String action,
                                                 @Field("v") String boardEngName);


    @FormUrlEncoded
    @Headers("X-Requested-With:XMLHttpRequest")
    @POST("/nForum/article/{boardEngName}/ajax_add_like/{topicID}.json")
    Observable<AjaxResponse> addLike(@Path("boardEngName") String boardEngName,
                                     @Path("topicID") String topicID,
                                     @Field("score") String score,
                                     @Field("msg") String msg,
                                     @Field("tag") String tag);
}
