package com.cookandroid.kotlin_project

import android.annotation.SuppressLint
import android.content.*
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.cookandroid.kotlin_project.backendinterface.dto.MemberDTO
import com.cookandroid.kotlin_project.databinding.ActivityChatApplicationBinding
import com.cookandroid.kotlin_project.localDB.database.UserDatabase
import com.cookandroid.kotlin_project.backendinterface.stomp.StompClientService
import com.cookandroid.kotlin_project.backendinterface.stomp.dto.StompChatDTO
import com.cookandroid.kotlin_project.localDB.database.MemberDatabase
import com.cookandroid.kotlin_project.localDB.database.StompChatDatabase
import java.util.stream.Collectors

class ChatApplication : AppCompatActivity() {

    internal lateinit var preferences: SharedPreferences
    var arraylist = arrayListOf<StompChatDTO>()
    val mAdapter = ChatAdapter(this,arraylist)

    private lateinit var stompService : StompClientService
    private var mStompServiceBound : Boolean = false;

    private lateinit var roomDB : UserDatabase
    private lateinit var chatDB : StompChatDatabase
    private lateinit var memberDB : MemberDatabase

    private val connection = object : ServiceConnection {

        override fun onServiceConnected(className: ComponentName, service: IBinder) {

            Log.d("Info", "Success to connect Stomp Service")

            val binder = service as StompClientService.LocalBinder
            stompService = binder.getService()
            mStompServiceBound = true
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mStompServiceBound = false
        }
    }

    private val broadcastReceiver: BroadcastReceiver = object:BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            Log.d("Receiver", "Intent: $intent")
            /*
            if(intent?.getStringExtra("flag_stomp").equals("chat_update")) {
                val user_data = mutableListOf<StompChatDTO>()
                chatDB.stompChatDao().getAll().forEach { chatEntity ->
                    val memberEntity = memberDB.memberDao().findByMemberId(chatEntity.senderId)
                    user_data.StompChatDTO(
                        sender = MemberDTO(
                            id = memberEntity.mid,
                            nickname = memberEntity.nickname,
                            isManager = memberEntity.isManager,
                            group = null,
                            stompToken = null),
                        sendTime = chatEntity.sendTime,
                        payload = chatEntity.message
                    ) }
                Thread {
                    mAdapter.setItem(user_data)
                    mAdapter.notifyDataSetChanged()

                    Log.d("DEBUG", "Success to receive message")
                }.start()
            }*/
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_application)

        roomDB = Room.databaseBuilder(applicationContext, UserDatabase::class.java, "UserTable").build()
        chatDB = Room.databaseBuilder(applicationContext, StompChatDatabase::class.java, "StompChatTable").build()
        memberDB = Room.databaseBuilder(applicationContext, MemberDatabase::class.java, "MemberTable").build()

        preferences = getSharedPreferences("email", Context.MODE_PRIVATE)

        val binding = ActivityChatApplicationBinding.inflate(layoutInflater)

        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        binding.recyclerview.adapter = mAdapter
        binding.recyclerview.setHasFixedSize(true)

        Log.d("DEBUG", "Oncreate")

        var chat_button : ImageButton = findViewById(R.id.messageActivity_ImageButton)
        var edit_text : EditText = findViewById(R.id.messageActivity_editText)
        chat_button.setOnClickListener {

            Log.d("DEBUG", "button clicked")

            val message : String = edit_text.getText().toString()
            edit_text.setText("")

            if(mStompServiceBound)
                stompService.sendChat(message = message, "2c9fa82c84c051f60184c054e0b70003")
        }

    }

    override fun onStart() {
        super.onStart()
        Intent(this, StompClientService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_EXTERNAL_SERVICE)
        }

        val intentFilter = IntentFilter()
        intentFilter.addAction("seniorsafe")
        registerReceiver(broadcastReceiver, intentFilter)
        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver, intentFilter)
    }

    override fun onDestroy() {
        unregisterReceiver(broadcastReceiver)
        super.onDestroy()
    }

    fun showToastMessage(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT)
    }
}