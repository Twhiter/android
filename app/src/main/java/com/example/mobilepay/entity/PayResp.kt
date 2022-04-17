package com.example.mobilepay.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class PayResp(
    @JsonProperty("prompt")val prompt:String,
    @JsonProperty("payOverview")val payOverview: PayOverview?
)
