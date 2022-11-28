package com.cookandroid.kotlin_project.localDB.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "MemberTable")
data class MemberEntity(
    @PrimaryKey(autoGenerate = true) var id: Int,
    @ColumnInfo(name = "mid") var mid: String,
    @ColumnInfo(name = "nickname") var nickname: String,
    @ColumnInfo(name = "isManager") var isManager: Boolean,
    @ColumnInfo(name = "token") var token: String,
    
    @ColumnInfo(name = "group") var group: GroupEntity
)
