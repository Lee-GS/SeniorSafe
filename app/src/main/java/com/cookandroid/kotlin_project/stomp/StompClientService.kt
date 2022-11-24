package com.cookandroid.kotlin_project.stomp

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import com.cookandroid.kotlin_project.backendinterface.dto.GroupDTO
import com.cookandroid.kotlin_project.backendinterface.dto.GroupTokenDTO
import com.cookandroid.kotlin_project.backendinterface.group.get_stompToken
import com.cookandroid.kotlin_project.backendinterface.group.group_get
import com.cookandroid.kotlin_project.stomp.dto.StompChatDTO
import com.cookandroid.kotlin_project.stomp.dto.StompGpsDTO
import com.google.gson.Gson
import com.google.gson.JsonObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.StompHeader
import ua.naiksoftware.stomp.dto.StompMessage


class StompClientService : Service() {

    private var server_url: String = "ws://backend.seniorsafe.tk:8080/ws-stomp"
    private var token: String = ""
    private var group_tokens = HashMap<String, GroupTokenDTO>()
    private var stompClient: StompClient ?= null

    private val api_group_get = group_get.create()
    private val api_group_stompToken = get_stompToken.create()
    private val gson = Gson()

    private val loginTokenIntentKey = "token_login"
    private val serverUrlIntentKey = "server_url"

    private val handlerThread = Handler(Looper.getMainLooper())

    private val binder = LocalBinder()

    inner class LocalBinder : Binder() {
        fun getService(): StompClientService = this@StompClientService
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d("StompService", "Starting stomp client service")

        // 인텐트 유효성 검사 및 초기화
        if(intent != null) {
            if (intent.hasExtra(loginTokenIntentKey))
                token = intent.getStringExtra(loginTokenIntentKey).toString()
            else
                stopSelf()
            if (intent?.hasExtra(serverUrlIntentKey) == true)
                server_url = intent?.getStringExtra(serverUrlIntentKey).toString()
        }
        else stopSelf()
        connectToStompServer()
        return super.onStartCommand(intent, flags, startId)
    }

    override fun onBind(p0: Intent?): IBinder? {
        Log.d("StompService", "onBind is running")

        if(p0 != null) {
            if (p0.hasExtra(loginTokenIntentKey))
                token = p0.getStringExtra(loginTokenIntentKey).toString()
            if (p0.hasExtra(serverUrlIntentKey))
                server_url = p0?.getStringExtra(serverUrlIntentKey).toString()
            connectToStompServer()
        }

        return binder
    }

    override fun onDestroy() {
        Log.d("StompService", "Stopping stomp client service")
        handlerThread.post(Runnable { stompClient!!.disconnectCompletable() })
        return super.onDestroy()
    }

    private fun connectToStompServer() {
        Log.d("StompService", "try connect to stomp server:$server_url with token:$token")

        if(stompClient != null)
            handlerThread.post(Runnable {
                stompClient!!.disconnect()
                group_tokens.clear()
            })

        handlerThread.post(Runnable {
            var headers = listOf(StompHeader("token", token))
            stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, server_url);
            stompClient!!.connect(headers)
        })

        getGroupInfoAndTokenAndSubscribeStomp()
    }

    private fun getGroupInfoAndTokenAndSubscribeStomp() {
        api_group_get.register("Bearer $token").enqueue(object : Callback<List<GroupDTO>> {
            override fun onResponse(call: Call<List<GroupDTO>>, response: Response<List<GroupDTO>>) {
                val result = response.code();
                if(result in 200..299) {
                    Log.d("api_group_get", response.body().toString())
                    val groupDTOs = response.body()!!.toList()
                    groupDTOs.forEach { groupDTO -> getGroupTokens(groupDTO.groupId) }
                }
                else {
                    Log.w("api_group_get", response.body().toString())
                }
            }

            override fun onFailure(call: Call<List<GroupDTO>>, t: Throwable) {
                Log.e("api_group_get","${t.localizedMessage}")
            }
        })
    }

    private fun getGroupTokens(groupId: String) {
        Log.d("getting group tokens", groupId)
        api_group_stompToken.register(BearerToken = "Bearer $token", groupId = groupId).enqueue(object : Callback<GroupTokenDTO> {
            override fun onResponse(call: Call<GroupTokenDTO>, response: Response<GroupTokenDTO>) {
                val result = response.code();
                if(result in 200..299) {
                    Log.d("api_group_token", response.body().toString())
                    subscribeStomp(response.body()!!)
                }
                else {
                    Log.w("api_group_token", response.toString())
                }
            }

            override fun onFailure(call: Call<GroupTokenDTO>, t: Throwable) {
                Log.e("api_group_token", t.localizedMessage)
            }
        })
    }

    @SuppressLint("CheckResult")
    private fun subscribeStomp(tokens: GroupTokenDTO) {
        handlerThread.post(Runnable {
            var headers = listOf(StompHeader("token", tokens.token))
            stompClient!!.topic("/sub/chat/${tokens.channelKey}", headers).subscribe { topicMessage ->
                Log.d("/sub/chat/${tokens.channelKey}", topicMessage.payload)
                var chatDTO = gson.fromJson(topicMessage.payload, StompChatDTO::class.java)
                Log.d("/sub/chat/${tokens.channelKey}", chatDTO.toString())
            }
            stompClient!!.topic("/sub/status/${tokens.channelKey}", headers).subscribe { topicMessage ->
                Log.d("/sub/status/${tokens.channelKey}", topicMessage.payload)

            }
            stompClient!!.topic("/sub/gps/${tokens.channelKey}", headers).subscribe { topicMessage ->
                Log.d("/sub/gps/${tokens.channelKey}", topicMessage.payload)
                var gpsDTO = gson.fromJson(topicMessage.payload, StompGpsDTO::class.java)
                Log.d("/sub/chat/${tokens.channelKey}", gpsDTO.toString())
            }
            group_tokens.add(tokens)
        })
    }

    fun sendGpsPos(latitude: Double, longitude: Double) {
        handlerThread.post(Runnable {
            group_tokens.forEach { tokens ->

                var header = listOf(
                    StompHeader("token", tokens.value.token),
                    StompHeader("destination", "/pub/gps/upload"))

                var payload = JsonObject()
                payload.addProperty("latitude", latitude.toString())
                payload.addProperty("longitude", longitude.toString())

                var stompMessage = StompMessage("MESSAGE", header, payload.toString())

                Log.d("debug", stompMessage.toString())
                stompClient!!.send(stompMessage).subscribe()
            }
        })
    }

    fun sendChat(requestDTO: StompChatDTO, tokenKey: String) {
        handlerThread.post(Runnable {
            if(group_tokens[tokenKey] != null){
                tokens ->
                    val token = group_tokens.get(tokenKey)?.token

                    var header = listOf(
                        StompHeader("token", group_tokens.get(tokenKey).token),
                        StompHeader("destination", "/pub/gps/upload")
                    )

                    var payload = JsonObject()
                    payload.addProperty("latitude", latitude.toString())
                    payload.addProperty("longitude", longitude.toString())

                    var stompMessage = StompMessage("MESSAGE", header, payload.toString())

                    Log.d("debug", stompMessage.toString())
                    stompClient!!.send(stompMessage).subscribe()
            }

        })
    }
}


        tokens ->
                val token = group_tokens.get(tokenKey)?.token

                var header = listOf(
                    StompHeader("token", group_tokens.get(tokenKey).token),
                    StompHeader("destination", "/pub/gps/upload")
                )

                var payload = JsonObject()
                payload.addProperty("latitude", latitude.toString())
                payload.addProperty("longitude", longitude.toString())

                var stompMessage = StompMessage("MESSAGE", header, payload.toString())

                Log.d("debug", stompMessage.toString())
                stompClient!!.send(stompMessage).subscribe()
