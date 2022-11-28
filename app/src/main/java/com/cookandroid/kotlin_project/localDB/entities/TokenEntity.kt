package com.cookandroid.kotlin_project.localDB.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "TokenTable")
data class TokenEntity(
    @PrimaryKey(autoGenerate = true) val id: Int,
    @ColumnInfo(name = "groupId") val groupId: Int,
    @ColumnInfo(name = "token") val token: String?,
    @ColumnInfo(name = "channelKey") val channelKey: String?,
    @ColumnInfo(name = "alertKey") val alertKey: String?,
    @ColumnInfo(name = "reqeustKey") val requestKey: String?
)