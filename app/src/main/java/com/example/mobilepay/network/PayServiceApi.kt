package com.example.mobilepay.network

import com.example.mobilepay.entity.PayResp
import com.example.mobilepay.entity.ResponseData
import retrofit2.http.*
import java.math.BigDecimal

interface PayServiceApi {

    @FormUrlEncoded
    @POST("/api/pay")
    suspend fun pay(
        @Header("token") token: String, @Field("merchantId") merchantId: Int,
        @Field("amount") amount: BigDecimal,
        @Field("paymentPassword") paymentPassword: String,
        @Field("remarks") remarks: String? = null,
    ): ResponseData<PayResp>

    @FormUrlEncoded
    @POST("/api/payWithConfirm")
    suspend fun payWithConfirm(
        @Header("token") token: String,
        @Field("sessionId") sessionId: Int,
        @Field("paymentPassword") paymentPassword: String,
        @Field("remarks") remarks: String? = null,
    ): ResponseData<PayResp>


    @PUT("/api/payment/state")
    suspend fun refundPay(@Header("token") token: String, @Body payId: Int): ResponseData<String>
}

object PayApi {
    val service: PayServiceApi by lazy {
        retrofit.create(PayServiceApi::class.java)
    }
}