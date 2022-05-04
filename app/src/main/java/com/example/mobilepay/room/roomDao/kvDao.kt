package com.example.mobilepay.room.roomDao

import androidx.room.*
import com.example.mobilepay.room.roomEntity.KV
import kotlinx.coroutines.flow.Flow

@Dao
interface KVDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun set(kv:KV)

    @Query("select value from KV where `key` = :key")
    suspend fun get(key:String):String?

    @Delete
    suspend fun delete(kv: KV)


    @Query("delete from KV")
    suspend fun deleteAll()
}


