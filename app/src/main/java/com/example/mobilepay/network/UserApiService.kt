package com.example.mobilepay.network

import com.example.mobilepay.entity.ResponseData
import okhttp3.MultipartBody
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part

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
}

object UserApi {
    val service:UserApiService by lazy {
        retrofit.create(UserApiService::class.java)
    }
}