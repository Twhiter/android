package com.example.mobilepay.room.roomDao

import androidx.room.*
import com.example.mobilepay.entity.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(user: User)

    @Update
    suspend fun update(user: User)


    @Query("select * from user where userId = :userId")
    fun get(userId: Int): Flow<User>

    @Delete
    suspend fun delete(user: User)
}