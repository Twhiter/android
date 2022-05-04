package com.example.mobilepay.network

import com.example.mobilepay.entity.ResponseData
import com.example.mobilepay.entity.TransferResp
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import java.math.BigDecimal

interface TransferServiceApi {


    @FormUrlEncoded
    @POST("/api/transfer")
    suspend fun transfer(
        @Header("token") token: String, @Field("targetUserId") targetUserId: Int,
        @Field("amount") amount: BigDecimal,
        @Field("paymentPassword") paymentPassword: String,
        @Field("remarks") remarks: String? = null,
    ): ResponseData<TransferResp>


}

object TransferApi {
    val service: TransferServiceApi by lazy {
        retrofit.create(TransferServiceApi::class.java)
    }
}