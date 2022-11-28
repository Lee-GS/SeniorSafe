package com.cookandroid.kotlin_project.localDB.database

import androidx.room.Database
import androidx.room.RoomDatabase
import com.cookandroid.kotlin_project.localDB.dao.MemberDAO
import com.cookandroid.kotlin_project.localDB.dao.TokenDAO
import com.cookandroid.kotlin_project.localDB.entities.MemberEntity

@Database(entities = [MemberEntity::class], version = 2)
abstract class MemberDatabase : RoomDatabase() {
    abstract fun memberDao(): MemberDAO
}