package com.example.mobilepay.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.fasterxml.jackson.annotation.JsonProperty
import java.math.BigDecimal


@Entity(tableName = "user")
data class User(
    @PrimaryKey @JsonProperty("userId") var userId: Int,
    @JsonProperty("phoneNumber") var phoneNumber: String,
    @JsonProperty("firstName") var firstName: String,
    @JsonProperty("lastName") var lastName: String,
    @JsonProperty("country") var country: String,
    @JsonProperty("email") var email: String,
    @JsonProperty("state") var state: String,
    @JsonProperty("moneyAmount") var moneyAmount: BigDecimal,
    @JsonProperty("frozenMoney") var frozenMoney: BigDecimal,
    @JsonProperty("avatar") var avatar: String,
) {


    companion object {
        //this is the default value for view model
        val mock = User(
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