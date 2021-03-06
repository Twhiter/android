package com.example.mobilepay.network

import com.example.mobilepay.entity.*
import okhttp3.MultipartBody
import retrofit2.http.*
import java.math.BigDecimal

interface UserApiService {


    @Multipart
    @POST("/api/user")
    suspend fun register(
        @Part country: MultipartBody.Part,
        @Part email: MultipartBody.Part,
        @Part firstName: MultipartBody.Part,
        @Part lastName: MultipartBody.Part,
        @Part passportNumber: MultipartBody.Part,
        @Part passportPhoto: MultipartBody.Part,
        @Part password: MultipartBody.Part,
        @Part paymentPassword: MultipartBody.Part,
        @Part phoneNumber: MultipartBody.Part,
    )
            : ResponseData<*>

    @POST("/api/token/user")
    suspend fun login(@Body phoneAndPwd: Map<String, String>): ResponseData<LoginResp>


    @GET("/api/user/self")
    suspend fun fetchInfo(@Header("token") token: String): ResponseData<User>


    @GET("/api/user/overview/{userId}")
    suspend fun fetchOverviewInfo(@Path("userId") userId: Int): ResponseData<OverviewInfo>


    @GET("/api/user/search")
    suspend fun searchUsers(
        @Query("keyword") keyword: String,
        @Query("page") page: Int,
        @Query("pageCount") pageSize: Int,
    ): ResponseData<Page<OverviewInfo>>


    @GET("/api/bills/user")
    suspend fun getUserBills(
        @Header("token") token: String,
        @Query("pageSize") pageSize: Int,
        @Query("pageNum") pageNum: Int,
        @Query("min") min: BigDecimal? = null,
        @Query("max") max: BigDecimal? = null,
        @Query("start") start: String? = null,
        @Query("end") end: String? = null,
        @Query("billTypes") billTypes: @JvmSuppressWildcards List<BillType>,
    ): ResponseData<Page<BillRecord>>


}

object UserApi {
    val service: UserApiService by lazy {
        retrofit.create(UserApiService::class.java)
    }
}