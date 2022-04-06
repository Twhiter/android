package com.example.mobilepay.room

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.mobilepay.MainApplication
import com.example.mobilepay.room.roomDao.KVDao
import com.example.mobilepay.room.roomEntity.KV

@Database(entities = [KV::class], version = 1, exportSchema = false)
abstract class AppDatabase:RoomDatabase() {

    abstract fun KVDao():KVDao


    companion object {
        @Volatile
        private var instance:AppDatabase? = null

        fun getInstance(context:Context):AppDatabase {

            if (instance != null)
                return instance!!
            else synchronized(this) {
                instance = Room.databaseBuilder(
                    context,
                    AppDatabase::class.java,
                    "mobile_pay"
                ).build()
                return instance!!
            }
        }
    }
}