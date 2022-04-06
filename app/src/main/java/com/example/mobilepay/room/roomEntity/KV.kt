package com.example.mobilepay.room.roomEntity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.annotation.NonNull

@Entity(tableName = "KV")
data class KV(
    @PrimaryKey  @ColumnInfo(name = "key", index = true) val key:String,
    @NonNull @ColumnInfo(name = "value")val value:String
)
