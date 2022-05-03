package com.example.mobilepay.entity

import android.content.Context
import android.widget.Toast
import androidx.core.content.ContentProviderCompat.requireContext
import com.fasterxml.jackson.annotation.JsonProperty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.xml.sax.helpers.DefaultHandler

data class ResponseData<T>(
    @JsonProperty("errorPrompt") val errorPrompt:String? = null,
    @JsonProperty("status") val status:Int,
    @JsonProperty("data") val data:T? = null
) {


    fun handle(vararg handlers: RespHandler<T>):Boolean {

        var b = true

        for (handler in handlers) {
            if (!b)
                break
            b = b && handler.handle(this)
        }
        return b
    }

    fun handleOneWithDefault(context:Context,handler: RespHandler<T>):Boolean =
        handle(defaultHandle(context,this),handler)

    fun handleDefault(context: Context):Boolean = handle(defaultHandle(context,this))




    companion object {

        const val OK:Int = 200
        const val ERROR = -1

        private fun<T> defaultHandle(context:Context,resp: ResponseData<T>):RespHandler<T> {

            return RespHandler {
                if (resp.status != OK) {
                    CoroutineScope(Dispatchers.Main).launch {
                        Toast.makeText(context,resp.errorPrompt,
                            Toast.LENGTH_SHORT).show()
                    }
                    false
                }else true
            }
        }


    }
}

fun interface RespHandler<T> {
    fun handle(responseData: ResponseData<T>):Boolean
}


