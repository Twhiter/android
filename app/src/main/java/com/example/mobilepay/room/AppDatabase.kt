package com.example.mobilepay.room

import android.content.Context
import androidx.room.*
import com.example.mobilepay.MainApplication
import com.example.mobilepay.entity.Merchant
import com.example.mobilepay.entity.User
import com.example.mobilepay.room.roomDao.KVDao
import com.example.mobilepay.room.roomDao.MerchantDao
import com.example.mobilepay.room.roomDao.UserDao
import com.example.mobilepay.room.roomEntity.KV
import java.math.BigDecimal

@TypeConverters(value = [Converters::class])
@Database(entities = [KV::class, Merchant::class,User::class], version = 1, exportSchema = false)
abstract class AppDatabase:RoomDatabase() {

    abstract fun kvDao():KVDao

    abstract fun merchantDao():MerchantDao

    abstract fun userDao():UserDao


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

class Converters {

    @TypeConverter
    fun fromBigDecimalToString(value:BigDecimal):String {
        return value.toPlainString()
    }

    @TypeConverter
    fun stringToBigDecimal(value:String):BigDecimal {
        return BigDecimal(value).setScale(4,BigDecimal.ROUND_HALF_UP)
    }
}
