package com.example.mobilepay.network

import com.example.mobilepay.entity.*
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query
import java.math.BigDecimal

interface MerchantApiService {

    @GET("/api/merchant/self")
    suspend fun fetchInfo(@Header("token")token:String):ResponseData<Merchant>

    @GET("/api/merchant/overview/{merchantId}")
    suspend fun fetchOverviewInfo(@Path("merchantId")merchantId:Int):ResponseData<OverviewInfo>

    @GET("/api/bill/merchant")
    suspend fun getMerchantBills(
        @Header("token") token:String,
        @Query("pageSize") pageSize:Int,
        @Query("pageNum") pageNum:Int,
        @Query("min") min: BigDecimal? = null,
        @Query("max") max: BigDecimal? = null,
        @Query("billTypes") billTypes:List<BillType>
    ):ResponseData<Page<BillRecord>>


}

object MerchantApi {
    val service:MerchantApiService by lazy {
        retrofit.create(MerchantApiService::class.java)
    }
}