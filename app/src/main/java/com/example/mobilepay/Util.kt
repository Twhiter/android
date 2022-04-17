package com.example.mobilepay
import android.content.Context
import android.text.InputFilter
import android.text.Spanned
import android.view.Gravity
import android.widget.LinearLayout
import com.example.mobilepay.entity.RespHandler
import com.example.mobilepay.entity.ResponseData
import com.example.mobilepay.network.MerchantApi
import com.example.mobilepay.network.UserApi
import com.fasterxml.jackson.databind.ObjectMapper
import com.lzj.pass.dialog.PayPassDialog
import com.lzj.pass.dialog.PayPassView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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

         fun showPayDialog(
             context: Context,
             title:String,
             forgetText:String,
             onPayClickListener: PayPassView.OnPayClickListener,
             dialog: PayPassDialog? = null) {

             val dialog2:PayPassDialog = dialog ?: PayPassDialog(context,R.style.dialog_pay_theme)

            dialog2.setAlertDialog(false)
                .setWindowSize(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,0.4f)
                .setOutColse(false)
                .setGravity(R.style.dialogOpenAnimation, Gravity.BOTTOM)

            dialog2.payViewPass
                .setHintText(title)
                .setForgetText(forgetText)
                .setPayClickListener(onPayClickListener)
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

class DecimalDigitsInputFilter(private val digitsBeforeZero: Int,
                               private val digitsAfterZero: Int):InputFilter {

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