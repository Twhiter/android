package com.example.mobilepay.room.roomDao

import androidx.room.*
import com.example.mobilepay.entity.Merchant
import kotlinx.coroutines.flow.Flow

@Dao
interface MerchantDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(merchant: Merchant)

    @Update
    suspend fun update(merchant: Merchant)

    @Query("select * from merchant where merchantUserId = :merchantUserId")
    fun get(merchantUserId:Int):Flow<Merchant>

    @Delete
    suspend fun delete(merchant: Merchant)
}