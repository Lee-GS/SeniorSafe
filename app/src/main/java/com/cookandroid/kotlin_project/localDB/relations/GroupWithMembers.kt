package com.cookandroid.kotlin_project.localDB.relations

import androidx.room.Embedded
import androidx.room.Relation
import com.cookandroid.kotlin_project.localDB.entities.GroupEntity
import com.cookandroid.kotlin_project.localDB.entities.MemberEntity
/*
data class GroupWithMembers(
    @Embedded val group: GroupEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "groupId"
    )
    var member: List<MemberEntity>
)
*/