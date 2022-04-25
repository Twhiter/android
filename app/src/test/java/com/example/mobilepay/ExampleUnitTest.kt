package com.example.mobilepay


import com.example.mobilepay.network.MerchantApi
import com.example.mobilepay.network.VerifyApi
import com.google.i18n.phonenumbers.PhoneNumberUtil
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.time.LocalDate
import java.time.Month
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle


/**
 * Example local unit test, which will execute on the development machine (host).
 *
 * See [testing documentation](http://d.android.com/tools/testing).
 */
class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(2+2,4)
        println("4")
    }

    @Test
     fun verifyTest() {

        val s = VerifyApi.service
        val m = HashMap<String,String>()
        m["type"] = "email"
        m["target"] = "whiterq6@gmail.com"


        runBlocking {
            val v = s.sendVerifyCode(m)
            println("${v.data} ${v.errorPrompt} ${v.status}")
        }
    }

    @Test
    fun checkPhone() {
        val phoneUtil = PhoneNumberUtil.getInstance()
        val swissNumberProto = phoneUtil.parse("+375445520140",null)
        println(phoneUtil.isPossibleNumber(swissNumberProto))


    }

    @Test
    fun test6() {
        runBlocking {
            println(MerchantApi.service.fetchInfo("52c60f37-83a0-4dd2-8485-387afebd2e5585bf4212-6906-3f40-b24b-eef922556dda"))
        }
    }

    @Test
    fun test7() {


        val dateFormatter = DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT)

        val dateOfBirth = LocalDate.of(1991, Month.OCTOBER, 13)
        val formattedDob = dateOfBirth.format(dateFormatter)
        println("Born: $formattedDob")

    }
}