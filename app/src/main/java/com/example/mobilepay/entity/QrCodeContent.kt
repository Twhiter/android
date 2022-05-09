package com.example.mobilepay.entity

import android.os.Parcelable
import com.fasterxml.jackson.annotation.JsonProperty
import com.fasterxml.jackson.annotation.JsonValue
import kotlinx.parcelize.Parcelize
import java.math.BigDecimal


@Parcelize
data class QrCodeContent(
    @JsonProperty("id") val id: Int,
    @JsonProperty("type") val type: Type,
    @JsonProperty("sessionId")val sessionId: Int? = null,
    @JsonProperty("amount")val amount:BigDecimal? = null
) : Parcelable

enum class Type(@JsonValue val value: String) {
    User("user"),
    Merchant("merchant"),
    MerchantWithConfirmation("merchantConfirmation")
}
