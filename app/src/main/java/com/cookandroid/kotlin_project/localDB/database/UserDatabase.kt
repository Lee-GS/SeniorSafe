package com.cookandroid.kotlin_project.localDB.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cookandroid.kotlin_project.localDB.entities.UserEntity
import com.cookandroid.kotlin_project.localDB.dao.UserDAO

@Database(entities = [UserEntity::class], version = 1)
abstract class UserDatabase : RoomDatabase() {
    abstract fun userDao(): UserDAO
}