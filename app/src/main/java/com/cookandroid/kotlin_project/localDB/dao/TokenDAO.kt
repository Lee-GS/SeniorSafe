package com.cookandroid.kotlin_project.localDB.dao

import androidx.annotation.NonNull
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.cookandroid.kotlin_project.localDB.entities.TokenEntity

@Dao
interface TokenDAO {

    @Query("SELECT * FROM TokenTable")
    fun getAll(): List<TokenEntity>

    @Query("SELECT * FROM TokenTable WHERE id IN (:tokenIds)")
    fun loadAllByIds(tokenIds: IntArray): List<TokenEntity>

    @Query("SELECT * FROM TokenTable WHERE groupId LIKE (:gid) LIMIT 1")
    fun findByGid(gid: String): TokenEntity

    @Insert
    fun insertAll(vararg tokens: TokenEntity)

    @Delete
    fun delete(user: TokenEntity)

    @Delete
    fun deleteAll(@NonNull entities: List<TokenEntity>)
}