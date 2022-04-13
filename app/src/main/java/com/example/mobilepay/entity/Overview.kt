package com.example.mobilepay.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class OverviewInfo (
    @JsonProperty("avatar")val avatar: String?,
    @JsonProperty("name")val name: String,
    @JsonProperty("phoneNumber")val phoneNumber:String
) {


    companion object {
        val mock = OverviewInfo(
            "",
            "",
            ""
        )
    }
}


