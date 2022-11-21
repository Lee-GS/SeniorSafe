package com.cookandroid.kotlin_project.stomp

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
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import ua.naiksoftware.stomp.Stomp
import ua.naiksoftware.stomp.StompClient
import ua.naiksoftware.stomp.dto.StompHeader


class StompClientService : Service() {

    private var server_url: String = "ws://kangtong1105.codns.com:8080/ws-stomp"
    private var token: String = ""
    private lateinit var stompClient: StompClient

    private val api_group_get = group_get.create()
    private val api_group_stompToken = get_stompToken.create()

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

            if(stompClient.isConnected())
                stompClient.disconnect()
            connectToStompServer()
        }

        return binder
    }

    override fun onDestroy() {
        Log.d("StompService", "Stopping stomp client service")
        handlerThread.post(Runnable { stompClient.disconnectCompletable() })
        return super.onDestroy()
    }

    private fun connectToStompServer() {
        Log.d("StompService", "try connect to stomp server:$server_url with token:$token")

        handlerThread.post(Runnable {
            var headers = listOf(StompHeader("token", token))
            stompClient = Stomp.over(Stomp.ConnectionProvider.OKHTTP, server_url);
            stompClient.connect(headers)
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
                Log.e("api_group_token","${t.localizedMessage}")
            }
        })
    }

    private fun subscribeStomp(tokens: GroupTokenDTO) {
        handlerThread.post(Runnable {
            var headers = listOf(StompHeader("token", tokens.token))
            stompClient.topic("/sub/chat/${tokens.channelKey}", headers).subscribe({ topicMessage ->
                Log.d("/sub/chat/${tokens.channelKey}", topicMessage.getPayload())
            })
            stompClient.topic("/sub/status/${tokens.channelKey}", headers).subscribe({ topicMessage ->
                Log.d("/sub/status/${tokens.channelKey}", topicMessage.getPayload())
            })
            stompClient.topic("/sub/gps/${tokens.channelKey}", headers).subscribe({ topicMessage ->
                Log.d("/sub/gps/${tokens.channelKey}", topicMessage.getPayload())
            })
        })
    }
}