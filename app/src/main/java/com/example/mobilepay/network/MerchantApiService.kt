package com.example.mobilepay.network

import android.provider.ContactsContract
import com.example.mobilepay.entity.*
import okhttp3.MultipartBody
import retrofit2.http.*
import java.math.BigDecimal
import java.sql.Timestamp
import java.util.*

interface MerchantApiService {

    @GET("/api/merchant/self")
    suspend fun fetchInfo(@Header("token")token:String):ResponseData<Merchant>

    @GET("/api/merchant/overview/{merchantId}")
    suspend fun fetchOverviewInfo(@Path("merchantId")merchantId:Int):ResponseData<OverviewInfo>

    @GET("/api/bills/merchant")
    suspend fun getMerchantBills(
        @Header("token") token:String,
        @Query("pageSize") pageSize:Int,
        @Query("pageNum") pageNum:Int,
        @Query("min") min: BigDecimal? = null,
        @Query("max") max: BigDecimal? = null,
        @Query("start") start: String? = null,
        @Query("end") end: String? = null,
        @Query("billTypes") billTypes:@JvmSuppressWildcards List<BillType>
    ):ResponseData<Page<BillRecord>>

    @Multipart
    @POST("/api/merchant")
    suspend fun register(
        @Header("token") token:String,
        @Part companyName:MultipartBody.Part,
        @Part licenseNumber:MultipartBody.Part,
        @Part licensePhoto: MultipartBody.Part,
        @Part phoneNumber: MultipartBody.Part? = null,
        @Part email:MultipartBody.Part? = null
    ):ResponseData<String?>


}

object MerchantApi {
    val service:MerchantApiService by lazy {
        retrofit.create(MerchantApiService::class.java)
    }
}