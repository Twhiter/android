package com.example.mobilepay.room.roomDao

import androidx.room.*
import com.example.mobilepay.room.roomEntity.SearchHistory
import kotlinx.coroutines.flow.Flow

@Dao
interface SearchHistoryDao {

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insert(searchHistory: SearchHistory)

    @Query("select * from search_history order by keyword")
    fun queryAll(): Flow<List<SearchHistory>>

    @Delete
    suspend fun delete(searchHistory: SearchHistory)

    @Query("delete from search_history")
    suspend fun deleteAll()
}