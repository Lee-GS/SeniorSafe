package com.cookandroid.kotlin_project.localDB.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cookandroid.kotlin_project.localDB.dao.TokenDAO
import com.cookandroid.kotlin_project.localDB.entities.TokenEntity

@Database(entities = [TokenEntity::class], version = 1)
abstract class TokenDatabase : RoomDatabase() {
    abstract fun tokenDao(): TokenDAO
}