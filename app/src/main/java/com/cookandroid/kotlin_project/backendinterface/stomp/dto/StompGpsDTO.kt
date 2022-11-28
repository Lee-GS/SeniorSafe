package com.cookandroid.kotlin_project.backendinterface.stomp.dto

import com.cookandroid.kotlin_project.backendinterface.dto.MemberDTO

data class StompGpsDTO(
    val sender: MemberDTO ?= null,
    val latitude: Double ?= 0.0,
    val longitude: Double ?= 0.0,
    val sendTime: String ?= null
)