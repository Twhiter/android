package com.example.mobilepay.ui.register

import android.content.Context
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import com.example.mobilepay.MainApplication
import com.example.mobilepay.R


data class PhoneCode( val countryName:String,val code: String) {


    companion object {
        val COUNTRY_CODES = getCodes()
        private fun getCodes():List<PhoneCode> {

            val context: Context = MainApplication.applicationContext()

            val ids = context.resources.obtainTypedArray(R.array.phoneCodes)
            val codes:MutableList<PhoneCode> = mutableListOf()


            for (i in 0 until ids.length()) {
                val id = ids.getResourceId(i,0)

                val arr = context.resources.getStringArray(id)
                codes.add(PhoneCode(arr[0],arr[1]))
            }
            ids.recycle()
            return codes
        }
    }
}





