package com.cookandroid.kotlin_project.localDB.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.cookandroid.kotlin_project.localDB.entities.GroupEntity
import com.cookandroid.kotlin_project.localDB.entities.TokenEntity
/*
data class GroupWithToken(
    @Embedded val group: GroupEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "groupId"
    )
    val members: TokenEntity
)
*/