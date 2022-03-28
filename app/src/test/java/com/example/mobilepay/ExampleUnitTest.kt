package com.example.mobilepay


import android.graphics.Region
import com.example.mobilepay.network.VerifyApi
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test


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


}