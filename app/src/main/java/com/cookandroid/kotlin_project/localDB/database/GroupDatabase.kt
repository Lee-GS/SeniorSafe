package com.cookandroid.kotlin_project.localDB.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cookandroid.kotlin_project.localDB.dao.GroupDAO
import com.cookandroid.kotlin_project.localDB.entities.GroupEntity

@Database(entities = [GroupEntity::class], version = 1)
abstract class GroupDatabase : RoomDatabase() {
    abstract fun groupDao(): GroupDAO
}