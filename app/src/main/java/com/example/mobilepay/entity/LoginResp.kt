package com.example.mobilepay.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class LoginResp(
    @JsonProperty("token") val token: String,
    @JsonProperty("isOkay") val isOkay: Boolean,
    @JsonProperty("prompt") val prompt: String,
) {

    companion object {
        fun checkIsOkay(prompt: String): Boolean {
            return prompt == ""
        }
    }
}
