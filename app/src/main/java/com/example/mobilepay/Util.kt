package com.example.mobilepay

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Matrix
import android.os.Environment
import android.text.InputFilter
import android.text.Spanned
import android.util.Log
import android.widget.Button
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.RecyclerView.Recycler
import com.example.mobilepay.entity.ResponseData
import com.example.mobilepay.network.MerchantApi
import com.example.mobilepay.network.UserApi
import com.fasterxml.jackson.databind.ObjectMapper
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import com.google.i18n.phonenumbers.Phonenumber
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.withContext
import java.io.File
import java.io.IOException
import java.math.BigDecimal
import java.math.RoundingMode
import java.text.SimpleDateFormat
import java.util.*
import java.util.regex.Matcher
import java.util.regex.Pattern


class Util {

    companion object {

        const val EMAIL_PATTERN =
            "^[a-zA-Z0-9.!#\$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9-]+(?:\\.[a-zA-Z0-9-]+)*\$"
        val billsRequestDateFormat = SimpleDateFormat("yyyy-MM-dd")
        val billsShowDateFormat = SimpleDateFormat("MMM dd hh:mm")

        fun decimalPattern(digitsBeforeZero: Int, digitsAfterZero: Int): Pattern {

            val str =
                "([0-9]{0,${digitsBeforeZero - 1}})((\\.[0-9]{0,${digitsAfterZero - 1}})?|(\\.)?)"
            return Pattern.compile(str)
        }

        fun toBillRequestDataFormat(date: Date?): String? {
            return date?.let {
                billsRequestDateFormat.format(it)
            }
        }

        @Throws(IOException::class)
        fun createImageFile(): File {

            // Create an image file name
            val timeStamp: String = SimpleDateFormat("yyyyMMdd_HHmmss").format(Date())
            val storageDir: File =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES)
            return File.createTempFile(
                "JPEG_${timeStamp}_", /* prefix */
                ".jpg", /* suffix */
                storageDir /* directory */
            )
        }

        @JvmStatic
        fun toBillShowDateFormat(date: Date?): String? {
            return date?.let {
                billsShowDateFormat.format(it)
            }
        }


        fun formatBigDecimalToStr(bigDecimal: BigDecimal) =
            bigDecimal.setScale(2, RoundingMode.HALF_DOWN).toString()

        inline fun <reified T> fromJsonToObject(jsonStr: String): T? {

            return try {
                ObjectMapper().readValue(jsonStr, T::class.java)
            } catch (e: Exception) {
                null
            }
        }

        suspend fun suspendSend(btn: Button) {
            withContext(Dispatchers.Main) {
                val text = btn.text
                btn.isEnabled = false
                for (i in 60 downTo 1) {
                    btn.text = i.toString()
                    delay(1000)
                }
                btn.text = text
                btn.isEnabled = true
            }
        }

        fun checkEmail(email: String): Boolean {
            val pattern = Regex(EMAIL_PATTERN)
            return email.matches(pattern)
        }


        suspend fun updateSelfInfo() {

            withContext(Dispatchers.IO) {

                try {
                    val token = MainApplication.db().kvDao().get("token")
                    token?.let {
                        val resp = UserApi.service.fetchInfo(it)
                        val isOkay = resp.handle({ r -> r.status == ResponseData.OK },
                            { r -> r.data != null })

                        if (!isOkay)
                            return@withContext

                        MainApplication.db().userDao().update(resp.data!!)

                        val resp1 = MerchantApi.service.fetchInfo(it)
                        val isOkay1 = resp.handle({ r -> r.status == ResponseData.OK },
                            { r -> r.data != null })

                        if (!isOkay1)
                            return@withContext

                        MainApplication.db().merchantDao().update(merchant = resp1.data!!)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }


        suspend fun tryUpdateSelfInfo(context: Context): Boolean {

            var i = 0
            while (i < 3) {

                try {

                    val token = MainApplication.db().kvDao().get("token") ?: throw Exception()
                    val resp = UserApi.service.fetchInfo(token)
                    if (!resp.handleDefault(context))
                        throw Exception()

                    MainApplication.db().userDao().insert(resp.data!!)
                    val resp1 = MerchantApi.service.fetchInfo(token)
                    resp1.handleDefault(context)

                    if (!resp1.handleDefault(context))
                        throw Exception()

                    MainApplication.db().merchantDao().insert(merchant = resp1.data!!)
                    break

                } catch (e: Exception) {
                    e.printStackTrace()
                    i++
                    delay(10 * 1000L)
                    continue
                }
            }

            return i != 3
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

            val resizeLogo =
                Bitmap.createScaledBitmap(logo, qrcode.width / 6, qrcode.height / 6, true)

            val centreX = (canvasWidth - resizeLogo.width) / 2
            val centreY = (canvasHeight - resizeLogo.height) / 2

            canvas.drawBitmap(resizeLogo, centreX.toFloat(), centreY.toFloat(), null)
            return combined
        }

        fun getQrCodeBitmap(content: String): Bitmap {
            val size = 512 //pixels

            val bits = QRCodeWriter().encode(content, BarcodeFormat.QR_CODE, size, size)

            return Bitmap.createBitmap(size, size, Bitmap.Config.RGB_565).also {
                for (x in 0 until size)
                    for (y in 0 until size)
                        it.setPixel(x, y, if (bits[x, y]) Color.BLACK else Color.WHITE)

            }
        }

        fun getQrCodeBitmapWithLogo(content: String, logo: Bitmap): Bitmap {
            return mergeBitmaps(logo, getQrCodeBitmap(content))
        }

        @Throws(NumberParseException::class)
        fun checkPhone(phoneNumber: String): Boolean {
            val phoneUtil = PhoneNumberUtil.getInstance()
            val phoneNUmber: Phonenumber.PhoneNumber?
            try {
                phoneNUmber = phoneUtil.parse(phoneNumber,
                    null)
            } catch (e: NumberParseException) {
                return false
            }
            return phoneUtil.isPossibleNumber(phoneNUmber)
        }


    }


}

class DecimalDigitsInputFilter(
    private val digitsBeforeZero: Int,
    private val digitsAfterZero: Int,
) : InputFilter {

    override fun filter(
        source: CharSequence?,
        start: Int,
        end: Int,
        dest: Spanned?,
        dstart: Int,
        dend: Int,
    ): CharSequence? {

        val matcher: Matcher = Util.decimalPattern(digitsBeforeZero, digitsAfterZero).matcher(dest)
        return if (!matcher.matches()) "" else null
    }
}

class WrapContentLinearLayoutManager(context: Context) : LinearLayoutManager(context) {

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

    private val datas: MutableList<Any?> =
        MutableList(liveDatas.size) { index -> liveDatas[index].value }

    init {
        for (i in liveDatas.indices) {
            super.addSource(liveDatas[i]) {
                datas[i] = it
                value = combine(datas)
            }
        }
    }
}


class Processor<T> {

    private val list = mutableListOf<ProcessHandler<T?>>()

    fun addHandler(handler: ProcessHandler<T?>): Processor<T> {
        list.add(handler)
        return this
    }

    fun process(): T? {
        list.forEach {
            val result = it.handle()
            if (result != null)
                return result
        }
        return null
    }
}

fun interface ProcessHandler<T> {
    fun handle(): T?
}

data class PhoneCode(val countryName: String, val code: String) {


    companion object {
        val COUNTRY_CODES = getCodes()
        private fun getCodes(): List<PhoneCode> {

            val context: Context = MainApplication.applicationContext()

            val ids = context.resources.obtainTypedArray(R.array.phoneCodes)
            val codes: MutableList<PhoneCode> = mutableListOf()


            for (i in 0 until ids.length()) {
                val id = ids.getResourceId(i, 0)

                val arr = context.resources.getStringArray(id)
                codes.add(PhoneCode(arr[0], arr[1]))
            }
            ids.recycle()
            return codes
        }
    }
}

