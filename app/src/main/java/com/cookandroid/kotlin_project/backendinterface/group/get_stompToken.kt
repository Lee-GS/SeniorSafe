package com.cookandroid.kotlin_project.backendinterface.group

import com.cookandroid.kotlin_project.backendinterface.dto.GroupTokenDTO
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Headers
import retrofit2.http.Path

interface get_stompToken {
    @GET("/group/{groupId}/token")
    @Headers("content-type: application/json",
        "accept: application/json")
    fun register(@Path("groupId") groupId: String, @Header("Authorization") BearerToken : String) : Call<GroupTokenDTO>

    companion object { // static 처럼 공유객체로 사용가능함. 모든 인스턴스가 공유하는 객체로서 동작함.
        private const val BASE_URL = "http://kangtong1105.codns.com:8080" // 주소

        fun create(): get_stompToken {
            val gson : Gson = GsonBuilder().setLenient().create();
            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(get_stompToken::class.java)
        }
    }
}