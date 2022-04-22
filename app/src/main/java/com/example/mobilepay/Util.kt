package com.example.mobilepay

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.example.mobilepay.entity.ResponseData
import com.example.mobilepay.network.MerchantApi
import com.example.mobilepay.network.UserApi
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
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

        /**
         * merge logo in the center of the qr code
         */
        fun mergeBitmaps(logo: Bitmap, qrcode: Bitmap): Bitmap {
            val combined = Bitmap.createBitmap(qrcode.width, qrcode.height, qrcode.config)
            val canvas = Canvas(combined)
            val canvasWidth = canvas.width
            val canvasHeight = canvas.height
            canvas.drawBitmap(qrcode, Matrix(), null)

            val resizeLogo = Bitmap.
            createScaledBitmap(logo, qrcode.width / 6, qrcode.height / 6, true)

            val centreX = (canvasWidth - resizeLogo.width) / 2
            val centreY = (canvasHeight - resizeLogo.height) / 2

            canvas.drawBitmap(resizeLogo, centreX.toFloat(), centreY.toFloat(), null)
            return combined
        }

        fun getQrCodeBitmap(content:String): Bitmap {
            val size = 512 //pixels

            val bits = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)

            return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
                for (x in 0 until size)
                    for (y in 0 until size)
                        it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)

            }
        }

        fun getQrCodeBitmapWithLogo(content:String,logo: Bitmap): Bitmap {
            return mergeBitmaps(logo, getQrCodeBitmap(content))
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

class CombinedLiveData<R>(
    vararg liveDatas: LiveData<*>,
    private val combine: (datas: List<Any?>) -> R,
) : MediatorLiveData<R>() {

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

