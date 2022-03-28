package com.example.mobilepay

import android.app.Application
import android.content.Context

class MainApplication:Application() {

    init {
        instance = this
    }

    companion object {
        private var instance: MainApplication? = null

        fun applicationContext() : Context {
            return instance!!.applicationContext
        }

       const val REQUEST_IMAGE_CAPTURE = 1
    }
}