package com.example.mobilepay.network

import com.example.mobilepay.entity.ResponseData
import com.example.mobilepay.entity.Type
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST
import java.math.BigDecimal

interface ExportAndImportServiceApi {

    @FormUrlEncoded
    @POST("/api/import/bank")
    suspend fun importFundsFromBank(@Header("token") token:String,
                                    @Field("userType") userType:String,
                                    @Field("amount") amount:BigDecimal, )
    :ResponseData<String>


    @FormUrlEncoded
    @POST("/api/export/bank")
    suspend fun exportFundsToBank(@Header("token") token:String,
                                    @Field("userType") userType:String,
                                    @Field("amount") amount:BigDecimal,
                                    @Field("paymentPassword") paymentPassword:String
    ):ResponseData<String>


    @FormUrlEncoded
    @POST("/api/export")
    suspend fun exportToMerchant(@Header("token") token:String,
                                 @Field("amount") amount: BigDecimal):ResponseData<String>

    @FormUrlEncoded
    @POST("/api/import")
    suspend fun importFromMerchant(@Header("token") token: String,
                                   @Field("amount") amount:BigDecimal):ResponseData<String>

}

object ExportAndImportApi {
    val service: ExportAndImportServiceApi by lazy {
        retrofit.create(ExportAndImportServiceApi::class.java)
    }
}