package com.cookandroid.kotlin_project

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.Room
import com.cookandroid.kotlin_project.databinding.ActivityChatApplicationBinding
import com.cookandroid.kotlin_project.localDB.database.UserDatabase
import com.cookandroid.kotlin_project.backendinterface.stomp.StompClientService
import com.cookandroid.kotlin_project.backendinterface.stomp.dto.StompChatDTO

class ChatApplication : AppCompatActivity() {

    internal lateinit var preferences: SharedPreferences
    var arraylist = arrayListOf<StompChatDTO>()
    val mAdapter = ChatAdapter(this,arraylist)

    private lateinit var stompService : StompClientService
    private var mStompServiceBound : Boolean = false;

    private lateinit var roomDB : UserDatabase

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_application)

        roomDB = Room.databaseBuilder(
            applicationContext,
            UserDatabase::class.java,
            "UserTable"
        ).build()

        preferences = getSharedPreferences("email", Context.MODE_PRIVATE)

        val binding = ActivityChatApplicationBinding.inflate(layoutInflater)

        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        binding.recyclerview.adapter = mAdapter
        binding.recyclerview.setHasFixedSize(true)


        binding.messageActivityImageButton.setOnClickListener{

            val chatDTO = StompChatDTO(
                payload = ActivityChatApplicationBinding.inflate(layoutInflater)
                    .messageActivityEditText.toString())

            //if(mStompServiceBound)
                //stompService.sendChat(chatDTO,)
            /*
            val user_data = StompChatDTO(

            )
            mAdapter.addItem(user_data)
            mAdapter.notifyDataSetChanged()
            binding.messageActivityEditText.setText("")
             */
        }

    }

    override fun onStart() {
        super.onStart()
        Intent(this, StompClientService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_EXTERNAL_SERVICE)
        }
    }
}