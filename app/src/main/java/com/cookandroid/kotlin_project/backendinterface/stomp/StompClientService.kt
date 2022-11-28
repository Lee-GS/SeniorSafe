package com.cookandroid.kotlin_project.backendinterface.stomp

import android.annotation.SuppressLint
import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.room.Room
import com.cookandroid.kotlin_project.backendinterface.dto.GroupDTO
import com.cookandroid.kotlin_project.backendinterface.dto.GroupTokenDTO
import com.cookandroid.kotlin_project.backendinterface.group.get_stompToken
import com.cookandroid.kotlin_project.backendinterface.group.group_get
import com.cookandroid.kotlin_project.backendinterface.stomp.dto.StompChatDTO
import com.cookandroid.kotlin_project.backendinterface.stomp.dto.StompGpsDTO
import com.cookandroid.kotlin_project.localDB.database.GroupDatabase
import com.cookandroid.kotlin_project.localDB.database.MemberDatabase
import com.cookandroid.kotlin_project.localDB.database.TokenDatabase
import com.cookandroid.kotlin_project.localDB.database.UserDatabase
import com.cookandroid.kotlin_project.localDB.entities.GroupEntity
import com.cookandroid.kotlin_project.localDB.entities.MemberEntity
import com.cookandroid.kotlin_project.localDB.entities.TokenEntity
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

    private var group_tokens = HashMap<String, GroupTokenDTO>()
    private var stompClient: StompClient ?= null
    private val gson = Gson()

    private lateinit var groupDB : GroupDatabase
    private lateinit var memberDB : MemberDatabase
    private lateinit var tokenDB : TokenDatabase

    private val api_group_get = group_get.create()
    private val api_group_stompToken = get_stompToken.create()

    private var server_url: String = "ws://3.35.24.135:8080/ws-stomp/websocket"
    private var token: String = ""
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

        Thread {
            groupDB = Room.databaseBuilder(applicationContext, GroupDatabase::class.java, "GroupTable").fallbackToDestructiveMigrationFrom().build()
            memberDB = Room.databaseBuilder(applicationContext, MemberDatabase::class.java, "MemberTable").fallbackToDestructiveMigrationFrom().build()
            tokenDB = Room.databaseBuilder(applicationContext, TokenDatabase::class.java, "TokenTable").fallbackToDestructiveMigrationFrom().build()

            groupDB.groupDao().deleteAll(groupDB.groupDao().getAll())
            memberDB.memberDao().deleteAll(memberDB.memberDao().getAll())
            tokenDB.tokenDao().deleteAll(tokenDB.tokenDao().getAll())
            var headers = listOf(StompHeader("token", token))
            stompClient = Stomp.over(Stomp.ConnectionProvider.JWS, server_url);
            stompClient!!.connect(headers)
        }.start()

        getGroupInfoAndTokenAndSubscribeStomp()
    }

    private fun getGroupInfoAndTokenAndSubscribeStomp() {
        api_group_get.register("Bearer $token").enqueue(object : Callback<List<GroupDTO>> {
            override fun onResponse(call: Call<List<GroupDTO>>, response: Response<List<GroupDTO>>) {
                val result = response.code();
                if(result in 200..299) {
                    Log.d("api_group_get", response.body().toString())
                    val groupDTOs = response.body()!!.toList()

                    groupDTOs.forEach { groupDTO -> getGroupTokens(groupDTO) }
                }
                else {
                    Log.w("api_group_get", response.body().toString())
                    stopSelf()
                }
            }

            override fun onFailure(call: Call<List<GroupDTO>>, t: Throwable) {
                Log.e("SubscribeStomp", t.localizedMessage)
                t.printStackTrace()
                stopSelf()
            }
        })
    }

    private fun getGroupTokens(groupDTO: GroupDTO) {
        Log.d("getting group tokens", groupDTO.groupId)
        api_group_stompToken.register(BearerToken = "Bearer $token", groupId = groupDTO.groupId).enqueue(object : Callback<GroupTokenDTO> {
            override fun onResponse(call: Call<GroupTokenDTO>, response: Response<GroupTokenDTO>) {
                val result = response.code();
                if(result in 200..299) {
                    Log.d("api_group_token", response.body().toString())
                    subscribeStomp(response.body()!!, groupDTO.groupId)

                    Thread {
                        groupDB.groupDao().insertAll(GroupEntity(
                            id = 0,
                            gid = groupDTO.groupId,
                            name = groupDTO.name))

                        val groupEntity = groupDB.groupDao().findByGid(groupDTO.groupId)

                        tokenDB.tokenDao().insertAll(TokenEntity(
                                id = 0,
                                groupId = groupEntity.id,
                                token = response.body()!!.token,
                                channelKey = response.body()!!.channelKey,
                                alertKey = response.body()!!.alertKey,
                                requestKey = response.body()!!.requestKey))

                        groupDTO.participants!!.forEach { memberDTO ->
                            memberDB.memberDao().insertAll(
                                MemberEntity(
                                    id = 0,
                                    mid = memberDTO.id!!,
                                    nickname = memberDTO.nickname!!,
                                    isManager = memberDTO.isManager!!,
                                    groupId = groupEntity.id))
                        }
                    }.start()
                }
                else {
                    Log.w("api_group_token", response.toString())
                }
            }

            override fun onFailure(call: Call<GroupTokenDTO>, t: Throwable) {
                Log.e("getGroupTokens", t.localizedMessage)
                t.printStackTrace()
            }
        })
    }

    @SuppressLint("CheckResult")
    private fun subscribeStomp(tokens: GroupTokenDTO, groupId: String) {
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
            group_tokens[groupId] = tokens
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

    fun sendChat(requestDTO: StompChatDTO, groupId: String) {
        handlerThread.post(Runnable {

            var header = listOf(
                StompHeader("token", group_tokens[groupId]?.token),
                StompHeader("destination", "/pub/chat"))

            var payload = gson.toJson(requestDTO)

            var stompMessage = StompMessage("MESSAGE", header, payload.toString())
            Log.d("debug", stompMessage.toString())
            stompClient!!.send(stompMessage).subscribe()
        })
    }
}
