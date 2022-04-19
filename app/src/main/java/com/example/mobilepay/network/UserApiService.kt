package com.example.mobilepay.network

import com.example.mobilepay.entity.LoginResp
import com.example.mobilepay.entity.OverviewInfo
import com.example.mobilepay.entity.ResponseData
import com.example.mobilepay.entity.User
import okhttp3.MultipartBody
import retrofit2.http.*

interface UserApiService {


    @Multipart
    @POST("/api/user")
    suspend fun register(@Part country: MultipartBody.Part
                         , @Part email:MultipartBody.Part
                         , @Part firstName:MultipartBody.Part
                         , @Part lastName:MultipartBody.Part
                         , @Part passportNumber:MultipartBody.Part
                         , @Part passportPhoto:MultipartBody.Part
                         , @Part password:MultipartBody.Part
                         , @Part paymentPassword:MultipartBody.Part
                         , @Part phoneNumber:MultipartBody.Part)
    :ResponseData<*>

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
    ): ResponseData<List<OverviewInfo>>

}

object UserApi {
    val service:UserApiService by lazy {
        retrofit.create(UserApiService::class.java)
    }
}