package com.example.mobilepay.network

import com.example.mobilepay.entity.Merchant
import com.example.mobilepay.entity.ResponseData
import retrofit2.http.GET
import retrofit2.http.Header

interface MerchantApiService {

    @GET("/api/merchant/self")
    suspend fun fetchInfo(@Header("token")token:String):ResponseData<Merchant>
}

object MerchantApi {
    val service:MerchantApiService by lazy {
        retrofit.create(MerchantApiService::class.java)
    }
}