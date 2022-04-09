package com.example.mobilepay
import java.math.BigDecimal
import java.math.RoundingMode

class Util {

    companion object {

        const val EMAIL_PATTERN = "^[a-zA-Z0-9.!#\$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*\$"


        fun formatBigDecimalToStr(bigDecimal: BigDecimal) =
            bigDecimal.setScale(2,RoundingMode.HALF_DOWN).toString()
    }


}