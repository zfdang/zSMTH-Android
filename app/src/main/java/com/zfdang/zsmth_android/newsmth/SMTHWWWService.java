package com.zfdang.zsmth_android.newsmth;

import java.util.Map;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;
import io.reactivex.Observable;

/**
 * Created by zfdang on 2016-3-16.
 */

public interface SMTHWWWService {

    @FormUrlEncoded
    @Headers("X-Requested-With:XMLHttpRequest")
    @POST("/nForum/user/ajax_login.json")
    Observable<AjaxResponse> login(
            @Field("id") String username, @Field("passwd") String password, @Field("CookieDate") String CookieDate);

    // {"ajax_st":1,"ajax_code":"0005","ajax_msg":"操作成功"}
    @Headers("X-Requested-With:XMLHttpRequest")
    @GET("/nForum/user/ajax_logout.json")
    Observable<AjaxResponse> logout();

    // 用户在收藏夹里创建的目录
    // http://www.newsmth.net/bbsfav.php?select=1
    @GET("/bbsfav.php")
    Observable<ResponseBody> getFavoriteBoardsInFolder(@Query("select") String path);

    // 收藏夹里的二级版面
    // https://www.newsmth.net/bbsdoc.php?board=SecondHand
    //   ==> bbsboa.php?group=5&group2=677
    @GET("/bbsdoc.php")
    Observable<ResponseBody> getFavoriteBoardsInSection(@Query("board") String boardEngName);

    @GET("/nForum/section/{section}?ajax")
    Observable<ResponseBody> getBoardsBySection(@Path("section") String section);

    // http://www.newsmth.net/nForum/board/FamilyLife?ajax&p=2
    @GET("/nForum/board/{boardEngName}?ajax")
    Observable<ResponseBody> getBoardTopicsByPage(@Path("boardEngName") String boardEngName,
                                                  @Query("p") String page);

    @Headers("X-Requested-With:XMLHttpRequest")
    @GET("/nForum/article/{boardEngName}/{topicID}?ajax")
    Observable<ResponseBody> getPostListByPage(@Path("boardEngName") String boardEngName, @Path("topicID") String topicID,
                                               @Query("p") int page, @Query("au") String author);

    @Headers("X-Requested-With:XMLHttpRequest")
    @GET("/nForum/mainpage?ajax")
    Observable<ResponseBody> getAllHotTopics();

    // http://www.newsmth.net/nForum/s/article?ajax&t1=ad&au=ad&m=on&a=on&b=WorkLife
    @Headers("X-Requested-With:XMLHttpRequest")
    @GET("/nForum/s/article?ajax")
    Observable<ResponseBody> searchTopicInBoard(
            @Query("t1") String keyword, @Query("au") String author, @Query("m") String elite, @Query("a") String attachment,
            @Query("b") String boardEngName);

    // the header line is important, because newsmth will ignore it without this header
    @Headers("X-Requested-With:XMLHttpRequest")
    @GET("/nForum/user/query/{username}.json")
    Observable<UserInfo> queryUserInformation(
            @Path("username") String username);

    @Headers({"Content-Type: application/octet-stream", "X-Requested-With:XMLHttpRequest"})
    @POST("/nForum/att/{boardEngName}/ajax_add.json")
    Observable<AjaxResponse> uploadAttachment(@Path("boardEngName") String boardEngName,
                                              @Query("name") String filename, @Body RequestBody fileContent);

    @FormUrlEncoded
    @Headers("X-Requested-With:XMLHttpRequest")
    @POST("/nForum/article/{boardEngName}/ajax_post.json")
    Observable<AjaxResponse> publishPost(@Path("boardEngName") String boardEngName, @Field("subject") String subject,
                                         @Field("content") String content, @Field("signature") String signature, @Field("id") String id);

    // http://www.newsmth.net/nForum/article/PocketLife/ajax_edit/2244172.json
    @FormUrlEncoded
    @Headers("X-Requested-With:XMLHttpRequest")
    @POST("/nForum/article/{boardEngName}/ajax_edit/{postID}.json")
    Observable<AjaxResponse> editPost(@Path("boardEngName") String boardEngName, @Path("postID") String postID,
                                      @Field("subject") String subject, @Field("content") String content);

    @FormUrlEncoded
    @Headers("X-Requested-With:XMLHttpRequest")
    @POST("/nForum/mail/{mailid}/ajax_send.json")
    Observable<AjaxResponse> sendMail(@Path("mailid") String mailid, @Field("id") String userid, @Field("title") String title,
                                      @Field("content") String content, @Field("signature") String signature, @Field("backup") String backup, @Field("num") String num);

    @Headers("X-Requested-With:XMLHttpRequest")
    @GET("/nForum/user/ajax_session.json")
    Observable<UserStatus> queryActiveUserStatus();

    @FormUrlEncoded
    @Headers("X-Requested-With:XMLHttpRequest")
    @POST("/nForum/fav/op/{favid}.json")
    Observable<AjaxResponse> manageFavoriteBoard(@Path("favid") String favid, @Field("ac") String action, @Field("v") String boardEngName);

    @FormUrlEncoded
    @Headers("X-Requested-With:XMLHttpRequest")
    @POST("/nForum/article/{boardEngName}/ajax_add_like/{topicID}.json")
    Observable<AjaxResponse> addLike(@Path("boardEngName") String boardEngName, @Path("topicID") String topicID, @Field("score") String score,
                                     @Field("msg") String msg, @Field("tag") String tag);

    @FormUrlEncoded
    @Headers("X-Requested-With:XMLHttpRequest")
    @POST("/nForum/article/{boardEngName}/ajax_forward/{postID}.json")
    Observable<AjaxResponse> forwardPost(@Path("boardEngName") String boardEngName, @Path("postID") String postID,
                                         @Field("target") String target, @Field("threads") String threads, @Field("noref") String noref, @Field("noatt") String noatt,
                                         @Field("noansi") String noansi);

    // http://www.newsmth.net/nForum/mail/inbox?ajax&p=2
    @Headers("X-Requested-With:XMLHttpRequest")
    @GET("/nForum/mail/{folder}?ajax")
    Observable<ResponseBody> getUserMails(
            @Path("folder") String folder, @Query("p") String page);

    // http://www.newsmth.net/nForum/refer/like?ajax&p=2
    @Headers("X-Requested-With:XMLHttpRequest")
    @GET("/nForum/refer/{folder}?ajax")
    Observable<ResponseBody> getReferPosts(
            @Path("folder") String folder, @Query("p") String page);

    // http://www.newsmth.net/nForum/refer/reply/ajax_read.json
    @FormUrlEncoded
    @Headers("X-Requested-With:XMLHttpRequest")
    @POST("/nForum/refer/{folder}/ajax_read.json")
    Observable<AjaxResponse> readReferPosts(@Path("folder") String folder, @Field("index") String mailId);

    // http://www.newsmth.net/nForum/mail/inbox/8.json
    @Headers("X-Requested-With:XMLHttpRequest")
    @GET("{mail_url}")
    Observable<AjaxResponse> getMailContent(@Path("mail_url") String mail_url);

    @FormUrlEncoded
    @Headers("X-Requested-With:XMLHttpRequest")
    @POST("/nForum/friend/ajax_add.json")
    Observable<AjaxResponse> addFriend(
            @Field("id") String userid);

    // http://www.newsmth.net/bbsdel.php?board=Test&id=910916
    @GET("/bbsdel.php")
    Observable<ResponseBody> deletePost(@Query("board") String boardEngName, @Query("id") String postID);

    // http://www.newsmth.net/nForum/mail/inbox/ajax_delete.json
    // http://www.newsmth.net/nForum/refer/reply/ajax_delete.json
    // m<mail_id>=on
    @FormUrlEncoded
    @Headers("X-Requested-With:XMLHttpRequest")
    @POST("/nForum/{type}/{folder}/ajax_delete.json")
    Observable<AjaxResponse> deleteMailOrReferPost(@Path("type") String type, @Path("folder") String folder,
                                                   @FieldMap Map<String, String> mail);

    // http://www.newsmth.net/bbsccc.php?do&board=DigiHome&id=575648
    // target=test&outgo=on
    @FormUrlEncoded
    @POST("/bbsccc.php?do")
    Observable<ResponseBody> repostPost(@Query("board") String boardEngName,
                                        @Query("id") String postID, @Field("target") String target, @Field("outgo") String outgo);

    @FormUrlEncoded
    @Headers("X-Requested-With:XMLHttpRequest")
    @POST("/nForum/article/{boardEngName}/ajax_manage/{postID}.json")
    Observable<AjaxResponse> deleteTopic(
            @Path("boardEngName") String boardEngName, @Path("postID") String postID,
            @Field("gid") String topicID, @Field("top") String top);

    @FormUrlEncoded
    @Headers("X-Requested-With:XMLHttpRequest")
    @POST("/nForum/article/{boardEngName}/ajax_deny/{postID}.json")
    Observable<AjaxResponse> banID(
            @Path("boardEngName") String boardEngName, @Path("postID") String postID,
            @Field("reason") String reason, @Field("day") Integer day);

    @FormUrlEncoded
    @Headers("X-Requested-With:XMLHttpRequest")
    @POST("/nForum/article/{boardEngName}/ajax_manage/{postID}.json")
    Observable<AjaxResponse> markPost(
            @Path("boardEngName") String boardEngName, @Path("postID") String postID,
            @Field("gid") String topicID, @Field("op") String op);

    @FormUrlEncoded
    @Headers("X-Requested-With:XMLHttpRequest")
    @POST("/nForum/article/{boardEngName}/ajax_manage/{postID}.json")
    Observable<AjaxResponse> readonlyTopic(
            @Path("boardEngName") String boardEngName, @Path("postID") String postID,
            @Field("gid") String topicID, @Field("top") String top);

    @GET("/nForum/board/NewExpress?ajax")
    Call<String> keepAlive();
}
