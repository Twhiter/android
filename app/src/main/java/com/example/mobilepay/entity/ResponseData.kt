package com.example.mobilepay.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class ResponseData<T>(
    @JsonProperty("errorPrompt") val errorPrompt:String? = null,
    @JsonProperty("status") val status:Int,
    @JsonProperty("data") val data:T? = null
) {


    companion object {

        const val OK:Int = 200
        const val ERROR = -1
    }
}

