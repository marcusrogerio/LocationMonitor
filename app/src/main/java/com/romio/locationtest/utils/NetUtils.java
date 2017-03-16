package com.romio.locationtest.utils;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.romio.locationtest.BuildConfig;
import com.romio.locationtest.data.net.NetConstants;

import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava.RxJavaCallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by roman on 3/8/17
 */

public class NetUtils {

    public static Retrofit getRxRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(NetConstants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create( prepareGson() ))
                .addCallAdapterFactory(RxJavaCallAdapterFactory.create())
                .client(getHttpClient())
                .build();
    }

    public static Retrofit getRetrofit() {
        return new Retrofit.Builder()
                .baseUrl(NetConstants.BASE_URL)
                .addConverterFactory(GsonConverterFactory.create( prepareGson() ))
                .client(getHttpClient())
                .build();
    }

    private static OkHttpClient getHttpClient() {
        OkHttpClient.Builder builder = new OkHttpClient.Builder()
                .connectTimeout(40, TimeUnit.SECONDS)
                .writeTimeout(40, TimeUnit.SECONDS)
                .readTimeout(40, TimeUnit.SECONDS);

        if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            builder.addNetworkInterceptor(loggingInterceptor);
        }

        return builder.build();
    }

    private static Gson prepareGson() {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.setDateFormat("EEE, dd MMM yyyy HH:mm:ss z");

        return gsonBuilder.create();
    }
}
