package com.zfdang.zsmth_android.newsmth;

import okhttp3.ResponseBody;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;
import rx.Observable;

/**
 * Created by zfdang on 2016-3-17.
 */
public interface SMTHMobileService {
    // this method is not used, now we only wServer.getAllHotTopics
//    @GET("/hot/{index}")
//    Observable<ResponseBody> getHotTopicsBySection(@Path("index") String index);

    @GET("/board/{boardEngName}")
    Observable<ResponseBody> getBoardTopicsByPage(@Path("boardEngName") String boardEngName, @Query("p") String page);
}
