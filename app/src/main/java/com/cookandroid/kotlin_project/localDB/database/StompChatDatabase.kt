package com.cookandroid.kotlin_project.localDB.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cookandroid.kotlin_project.localDB.dao.StompChatDAO
import com.cookandroid.kotlin_project.localDB.entities.StompChatEntity

@Database(entities = [StompChatEntity::class], version = 1)
abstract class StompChatDatabase : RoomDatabase() {
    abstract fun stompChatDao(): StompChatDAO
}