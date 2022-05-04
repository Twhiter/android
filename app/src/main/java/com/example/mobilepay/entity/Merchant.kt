package com.example.mobilepay.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal

@Entity(tableName = "merchant")
data class Merchant(
    @PrimaryKey @JsonProperty("merchantId") val merchantId: Int,
    @JsonProperty("merchantUserId") val merchantUserId: Int,
    @JsonProperty("merchantName") val merchantName: String,
    @JsonProperty("merchantLicense") val merchantLicense: String,
    @JsonProperty("merchantLicensePhoto") val merchantLicensePhoto: String,
    @JsonProperty("merchantPhoneNumber") val merchantPhoneNumber: String,
    @JsonProperty("merchantLogo") val merchantLogo: String?,
    @JsonProperty("merchantEmail") val merchantEmail: String,
    @JsonProperty("frozenMoney") val frozenMoney: BigDecimal,
    @JsonProperty("moneyAmount") val moneyAmount: BigDecimal,
    @JsonProperty("state") val state: String,
) {


    companion object {
        //this is the default value for view model
        val mock = Merchant(
            -1,
            -1,
            "",
            "",
            "",
            "",
            "",
            "",
            BigDecimal.ZERO,
            BigDecimal.ZERO,
            ""
        )
    }


}