package com.example.mobilepay

import android.content.Context
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.view.Gravity
import android.widget.LinearLayout
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.example.mobilepay.entity.ResponseData
import com.example.mobilepay.network.MerchantApi
import com.example.mobilepay.network.UserApi
import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.math.BigDecimal
import java.math.RoundingMode
import java.util.regex.Matcher
import java.util.regex.Pattern


class Util {

    companion object {

        const val EMAIL_PATTERN = "^[a-zA-Z0-9.!#\$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*\$"

        fun decimalPattern( digitsBeforeZero:Int, digitsAfterZero:Int):Pattern {

            val str = "([0-9]{0,${digitsBeforeZero - 1}})((\\.[0-9]{0,${digitsAfterZero - 1}})?|(\\.)?)"
           return Pattern.compile(str)
        }



        fun formatBigDecimalToStr(bigDecimal: BigDecimal) =
            bigDecimal.setScale(2,RoundingMode.HALF_DOWN).toString()

        inline fun<reified T> fromJsonToObject(jsonStr:String): T? {

            return try {
                ObjectMapper().readValue(jsonStr,T::class.java)
            }catch (e: Exception){
                null
            }
        }

        suspend fun updateSelfInfo() {

            withContext(Dispatchers.IO) {

                try {
                    val token = MainApplication.db().kvDao().get("token")
                    token?.let {
                        val resp = UserApi.service.fetchInfo(it)
                       val isOkay = resp.handle({ r-> r.status == ResponseData.OK},
                           {r -> r.data != null })

                        if (!isOkay)
                            return@withContext

                        MainApplication.db().userDao().update(resp.data!!)

                        val resp1 = MerchantApi.service.fetchInfo(it)
                        val isOkay1 = resp.handle({ r-> r.status == ResponseData.OK},
                            {r -> r.data != null })

                        if (!isOkay1)
                            return@withContext

                        MainApplication.db().merchantDao().update(merchant = resp1.data!!)
                    }
                }catch (e:Exception) {
                    e.printStackTrace()
                }
            }
        }
    }
}

class DecimalDigitsInputFilter(
    private val digitsBeforeZero: Int,
    private val digitsAfterZero: Int,
):InputFilter {

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int,
    ): CharSequence? {

        val matcher: Matcher = Util.decimalPattern(digitsBeforeZero,digitsAfterZero).matcher(dest)
        return if (!matcher.matches()) "" else null
    }
}

class WrapContentLinearLayoutManager(context:Context) : LinearLayoutManager(context) {

    override fun onLayoutChildren(recycler: Recycler, state: RecyclerView.State) {
        try {
            super.onLayoutChildren(recycler, state)
        } catch (e: IndexOutOfBoundsException) {
            Log.e("Error", "IndexOutOfBoundsException in RecyclerView happens")
        }
    }
}

class CombinedLiveData<R>(vararg liveDatas: LiveData<*>,
                          private val combine: (datas: List<Any?>) -> R) : MediatorLiveData<R>() {

    private val datas: MutableList<Any?> = MutableList(liveDatas.size) { index -> liveDatas[index].value }

    init {
        for(i in liveDatas.indices){
            super.addSource(liveDatas[i]) {
                datas[i] = it
                value = combine(datas)
            }
        }
    }
}