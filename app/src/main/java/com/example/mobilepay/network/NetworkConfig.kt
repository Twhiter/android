package com.example.mobilepay.network

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.jackson.JacksonConverterFactory
import java.util.concurrent.TimeUnit

const val BASE_URL = "http://10.0.2.2:8080"

val client = OkHttpClient.Builder()
    .readTimeout(60, TimeUnit.SECONDS)
    .build()


val retrofit: Retrofit = Retrofit.Builder()
    .addConverterFactory(JacksonConverterFactory.create())
    .baseUrl(BASE_URL)
    .client(client)
    .build()