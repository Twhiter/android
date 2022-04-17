package com.example.mobilepay.network

import com.example.mobilepay.entity.PayOverview
import com.example.mobilepay.entity.PayResp
import com.example.mobilepay.entity.ResponseData
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import java.math.BigDecimal

interface PayServiceApi {

    @FormUrlEncoded
    @POST("/api/pay")
    suspend fun pay(
        @Header("token") token: String, @Field("merchantId") merchantId: Int,
        @Field("amount") amount: BigDecimal,
        @Field("paymentPassword") paymentPassword: String,
        @Field("remarks") remarks:String? = null
    ):ResponseData<PayResp>
}

object PayApi {
    val service:PayServiceApi by lazy {
        retrofit.create(PayServiceApi::class.java)
    }
}