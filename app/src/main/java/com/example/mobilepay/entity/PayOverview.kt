package com.example.mobilepay.entity

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.util.*

data class PayOverview(
    @JsonProperty("payId") val payId: Int,
    @JsonProperty("userId") var userId: Int,
    @JsonProperty("merchantId") val merchantId: Int,
    @JsonProperty("amount") val amount: BigDecimal,
    @JsonProperty("time") val time: Date,
    @JsonProperty("state") val state: String,
    @JsonProperty("remarks") val remarks: String?,
)
