package com.cookandroid.kotlin_project.localDB.dao;

import androidx.annotation.NonNull
import androidx.room.*
import com.cookandroid.kotlin_project.localDB.entities.GroupEntity

//import com.cookandroid.kotlin_project.localDB.entities.MemberEntity;
//import com.cookandroid.kotlin_project.localDB.relations.GroupWithMembers
//import com.cookandroid.kotlin_project.localDB.relations.GroupWithToken

import java.util.List;

@Dao
interface GroupDAO {

    @Query("SELECT * FROM GroupTable")
    fun getAll(): List<GroupEntity>

    @Query("SELECT * FROM GroupTable WHERE id IN (:ids)")
    fun loadAllByIds(ids: IntArray): List<GroupEntity>

    @Query("SELECT * FROM GroupTable WHERE gid LIKE (:groupId) LIMIT 1")
    fun findByGid(groupId: String): GroupEntity

    @Insert
    fun insertAll(vararg group: GroupEntity)

    @Delete
    fun delete(user: GroupEntity)

    @Delete
    fun deleteAll(@NonNull entities: List<GroupEntity>)
/*
    @Transaction
    @Query("SELECT * FROM GroupTable WHERE gid LIKE (:groupId)")
    fun getGroupWithMembers(groupId: String): List<GroupWithMembers>

    @Transaction
    @Query("SELECT * FROM GroupTable WHERE gid LIKE (:groupId) LIMIT 1")
    fun getGroupWithToken(groupId: String): GroupWithToken
*/
}
