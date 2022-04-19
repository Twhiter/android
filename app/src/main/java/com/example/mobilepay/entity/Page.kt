package com.example.mobilepay.entity

import com.fasterxml.jackson.annotation.JsonProperty

data class Page<T>(
    @JsonProperty("currentPage")val currentPage:Int,
    @JsonProperty("maxPage")val maxPage:Int,
    @JsonProperty("pageSize")val pageSize:Int,
    @JsonProperty("data")var data: List<T>
)