package com.example.mobilepay.entity

import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal
import java.util.*

data class Transfer(
    @JsonProperty("transferId") val transferId: Int,
    @JsonProperty("sourceUserId") val sourceUserId: Int,
    @JsonProperty("targetUserId") val targetUserId: Int,
    @JsonProperty("time") val time: Date,
    @JsonProperty("amount") val amount: BigDecimal,
    @JsonProperty("remarks") val remarks: String?,
)