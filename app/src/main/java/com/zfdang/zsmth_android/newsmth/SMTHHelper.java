package com.zfdang.zsmth_android.newsmth;

import okhttp3.OkHttpClient;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

/**
 * Created by zfdang on 2016-3-16.
 */
public class SMTHHelper {

    public SMTHService smthService= null;
    private Retrofit retrofit = null;
    private final String SMTH_MOBILE_URL = "http://m.newsmth.net";

    public SMTHHelper() {

        retrofit = new Retrofit.Builder()
                .baseUrl(SMTH_MOBILE_URL)
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .addConverterFactory(ScalarsConverterFactory.create())
//                .addConverterFactory(GsonConverterFactory.create())
                .client(new OkHttpClient())
                .build();

        smthService = retrofit.create(SMTHService.class);
    }
//    public
}
