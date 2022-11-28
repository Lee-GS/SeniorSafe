package com.cookandroid.kotlin_project.backendinterface.group

import com.cookandroid.kotlin_project.backendinterface.auth.signin
import com.cookandroid.kotlin_project.backendinterface.dto.MemberDTO
import com.cookandroid.kotlin_project.backendinterface.dto.UserDTO
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface member_join {
    @POST("/group/member/join")
    @Headers("content-type: application/json",
        "accept: application/json")
    fun register(@Body jsonparams: MemberDTO) : Call<MemberDTO>

    companion object { // static 처럼 공유객체로 사용가능함. 모든 인스턴스가 공유하는 객체로서 동작함.
        private const val BASE_URL = "http://backend.seniorsafe.tk" // 주소

        fun create(): member_join {

            val gson : Gson = GsonBuilder().setLenient().create();

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(member_join::class.java)
        }
    }
}