package com.example.mobilepay

import android.app.Application
import android.content.Context
import com.example.mobilepay.room.AppDatabase

class MainApplication : Application() {


    init {
        instance = this
    }

    companion object {

        private val db: AppDatabase by lazy { AppDatabase.getInstance(instance!!) }

        private var instance: MainApplication? = null

        fun applicationContext(): Context {
            return instance!!.applicationContext
        }

        fun application(): MainApplication {
            return instance!!
        }

        fun db(): AppDatabase {
            return db
        }
    }
}