package com.cookandroid.kotlin_project.localDB.dao

import androidx.annotation.NonNull
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.cookandroid.kotlin_project.localDB.entities.StompChatEntity
import java.util.List

@Dao
interface StompChatDAO {
    @Query("SELECT * FROM StompChatTable")
    fun getAll(): List<StompChatEntity>

    @Insert
    fun insertAll(vararg group: StompChatEntity)

    @Delete
    fun delete(user: StompChatEntity)

    @Delete
    fun deleteAll(@NonNull entities: List<StompChatEntity>)
}