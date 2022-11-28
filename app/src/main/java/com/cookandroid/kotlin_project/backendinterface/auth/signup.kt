package com.cookandroid.kotlin_project.backendinterface.auth

import com.cookandroid.kotlin_project.backendinterface.dto.UserDTO
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import retrofit2.Call
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface signup {
    @POST("/auth/signup")
    @Headers("content-type: application/json",
        "accept: application/json")
    fun register(@Body jsonparams: UserDTO) : Call<UserDTO>

    companion object { // static 처럼 공유객체로 사용가능함. 모든 인스턴스가 공유하는 객체로서 동작함.
        private const val BASE_URL = "http://backend.seniorsafe.tk" // 주소

        fun create(): signup {

            val gson : Gson = GsonBuilder().setLenient().create();

            return Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
                .create(signup::class.java)
        }
    }
}