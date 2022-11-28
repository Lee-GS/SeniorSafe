package com.cookandroid.kotlin_project.localDB.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "StompChatTable")
data class StompChatEntity (
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo val senderId: String,
    @ColumnInfo val sendTime: String,
    @ColumnInfo val message: String
)