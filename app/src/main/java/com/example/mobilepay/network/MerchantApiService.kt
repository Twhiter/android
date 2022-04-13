package com.example.mobilepay.network

import com.example.mobilepay.entity.Merchant
import com.example.mobilepay.entity.OverviewInfo
import com.example.mobilepay.entity.ResponseData
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path

interface MerchantApiService {

    @GET("/api/merchant/self")
    suspend fun fetchInfo(@Header("token")token:String):ResponseData<Merchant>

    @GET("/api/merchant/overview/{merchantId}")
    suspend fun fetchOverviewInfo(@Path("merchantId")merchantId:Int):ResponseData<OverviewInfo>


}

object MerchantApi {
    val service:MerchantApiService by lazy {
        retrofit.create(MerchantApiService::class.java)
    }
}