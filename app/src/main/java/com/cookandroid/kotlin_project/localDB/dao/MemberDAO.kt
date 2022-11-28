package com.cookandroid.kotlin_project.localDB.dao;

import androidx.annotation.NonNull
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

import com.cookandroid.kotlin_project.localDB.entities.MemberEntity;

import java.util.List;

@Dao
interface MemberDAO {

    @Query("SELECT * FROM MemberTable")
    fun getAll(): List<MemberEntity>

    @Query("SELECT * FROM MemberTable WHERE id IN (:ids)")
    fun loadAllByIds(ids: IntArray): List<MemberEntity>

    @Query("SELECT * FROM MemberTable WHERE mid LIKE (:memberId) LIMIT 1")
    fun findByMemberId(memberId: String): MemberEntity

    @Insert
    fun insertAll(vararg tokens: MemberEntity)

    @Delete
    fun delete(user: MemberEntity)

    @Delete
    fun deleteAll(@NonNull entities: List<MemberEntity>)
}
