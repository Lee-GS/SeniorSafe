package com.cookandroid.kotlin_project.localDB.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "GroupTable")
data class GroupEntity(
    @PrimaryKey(autoGenerate = true) var id: Int,
    @ColumnInfo(name = "gid") var gid: String,
    @ColumnInfo(name = "name") var name: String?,
)