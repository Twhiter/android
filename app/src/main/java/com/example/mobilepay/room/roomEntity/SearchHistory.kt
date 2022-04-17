package com.example.mobilepay.room.roomEntity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "search_history")
data class SearchHistory(
    @PrimaryKey(autoGenerate = true) val id:Int,
     val keyword:String
)
