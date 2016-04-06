package com.zfdang.zsmth_android.newsmth;


import okhttp3.ResponseBody;
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
//    @FormUrlEncoded
//    @POST("/bbslogin.php")
//    Observable<ResponseBody> login(@Field("id") String username, @Field("passwd") String password);

    @FormUrlEncoded
    @POST("/bbslogin.php")
    Observable<ResponseBody> loginWithKick(@Field("id") String username, @Field("passwd") String password, @Field("kick_multi") String kickID);

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

    // the header line is important, because newsmth will ignore it without this header
    @Headers("X-Requested-With:XMLHttpRequest")
    @GET("/nForum/user/query/{username}.json")
    Observable<UserInfo> queryUserInformation(@Path("username") String username);
    
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

}
