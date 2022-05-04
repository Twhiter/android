package com.example.mobilepay.network

import com.example.mobilepay.entity.ResponseData
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface VerifyApiService {

    @POST("api/verifyCode")
    suspend fun sendVerifyCode(@Body requestData: Map<String, String>): ResponseData<*>

    @GET("api/verifyCode")
    suspend fun checkVerifyCode(
        @Query("type") type: String,
        @Query("target") target: String,
        @Query("code") code: String,
    ): ResponseData<Boolean>

    companion object {

        suspend fun sendVerifyCode(type: String, target: String): ResponseData<*> {

            val m = HashMap<String, String>()
            m["type"] = type
            m["target"] = target

            return VerifyApi.service.sendVerifyCode(m)
        }

    }
}

object VerifyApi {
    val service: VerifyApiService by lazy {
        retrofit.create(VerifyApiService::class.java)
    }
}
