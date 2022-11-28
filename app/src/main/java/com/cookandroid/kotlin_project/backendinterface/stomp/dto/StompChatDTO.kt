package com.cookandroid.kotlin_project.backendinterface.stomp.dto

import com.cookandroid.kotlin_project.backendinterface.dto.MemberDTO
import com.google.gson.annotations.Expose


data class StompChatDTO(
    @Expose(serialize = false)
    val sender: MemberDTO ?= null,
    @Expose(serialize = false)
    val sendTime: String ?= null,
    val payload: String ?= null
)
