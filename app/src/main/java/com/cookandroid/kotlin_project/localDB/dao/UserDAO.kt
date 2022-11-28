package com.cookandroid.kotlin_project.localDB.dao

import androidx.annotation.NonNull
import androidx.room.*
import com.cookandroid.kotlin_project.localDB.entities.UserEntity

@Dao
interface UserDAO {

    @Query("SELECT * FROM UserTable")
    fun getAll(): List<UserEntity>

    @Query("SELECT * FROM UserTable WHERE uid IN (:userIds)")
    fun loadAllByIds(userIds: IntArray): List<UserEntity>

    @Query("SELECT * FROM UserTable WHERE email LIKE :email LIMIT 1")
    fun findByEmail(email: String): UserEntity

    @Query("SELECT * FROM UserTable WHERE realname LIKE :realname LIMIT 1")
    fun findByRealname(realname: String): UserEntity

    @Query("SELECT * FROM UserTable WHERE username LIKE :username LIMIT 1")
    fun findByUsername(username: String): UserEntity

    @Insert
    fun insertAll(vararg users: UserEntity)

    @Delete
    fun delete(user: UserEntity)

    @Delete
    fun deleteAll(@NonNull entities: List<UserEntity>)
}