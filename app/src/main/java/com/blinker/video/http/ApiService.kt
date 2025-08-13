package com.blinker.video.http

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

/**
 * @author jiangshiyu
 * @date 2024/12/14
 */
object ApiService {

    private val okhttpClient = OkHttpClient.Builder()
        .addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY))
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("http://192.168.5.48:9992/video/")
        .client(okhttpClient)
        .addConverterFactory(GsonConvertFactory())
        .build()

    fun getService(): IApiInterface {
        return retrofit.create(IApiInterface::class.java)
    }
}