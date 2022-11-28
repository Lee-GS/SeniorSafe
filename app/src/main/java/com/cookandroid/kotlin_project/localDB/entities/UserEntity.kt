package com.cookandroid.kotlin_project.localDB

import androidx.room.AutoMigration
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "UserTable")
data class UserEntity(
    @PrimaryKey val uid: Int,
    @ColumnInfo(name = "email") val email: String?,
    @ColumnInfo(name = "username") val username: String?,
    @ColumnInfo(name = "realname") val realname: String?
    )
