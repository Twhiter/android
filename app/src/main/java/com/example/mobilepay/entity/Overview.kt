package com.example.mobilepay.entity

import com.fasterxml.jackson.annotation.JsonProperty


data class OverviewInfo (
    @JsonProperty("id") val id:Int,
    @JsonProperty("type") val type: String,
    @JsonProperty("avatar")val avatar: String?,
    @JsonProperty("name")val name: String,
    @JsonProperty("phoneNumber")val phoneNumber:String,
    @JsonProperty("email") val email:String
) {


    companion object {
        val mock = OverviewInfo(
            -1,
            "",
            "",
            "",
            "",
            ""
        )
    }
}


