package com.example.mobilepay.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class TransferResp(
    @JsonProperty("prompt")val prompt:String,
    @JsonProperty("transfer")val transfer:Transfer?
)
