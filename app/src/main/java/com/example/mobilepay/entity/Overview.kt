package com.example.mobilepay.entity

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.parcelize.Parcelize


@Parcelize
data class OverviewInfo (
    @JsonProperty("id") val id:Int,
    @JsonProperty("type") val type: String,
    @JsonProperty("avatar")val avatar: String?,
    @JsonProperty("name")val name: String,
    @JsonProperty("phoneNumber")val phoneNumber:String,
    @JsonProperty("email") val email:String
) : Parcelable {


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


