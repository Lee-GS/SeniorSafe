package com.cookandroid.kotlin_project

import android.content.*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.util.Log
import android.view.View
import android.widget.Adapter
import android.widget.LinearLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.cookandroid.kotlin_project.databinding.ActivityChatApplicationBinding
import com.cookandroid.kotlin_project.stomp.StompClientService
import com.cookandroid.kotlin_project.stomp.dto.StompChatDTO

class ChatApplication : AppCompatActivity() {

    internal lateinit var preferences: SharedPreferences
    var arraylist = arrayListOf<StompChatDTO>()
    val mAdapter = ChatAdapter(this,arraylist)

    private lateinit var stompService : StompClientService
    private var mStompServiceBound : Boolean = false;

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

        preferences = getSharedPreferences("email", Context.MODE_PRIVATE)

        val binding = ActivityChatApplicationBinding.inflate(layoutInflater)

        binding.recyclerview.layoutManager = LinearLayoutManager(this)
        binding.recyclerview.adapter = mAdapter
        binding.recyclerview.setHasFixedSize(true)


        binding.messageActivityImageButton.setOnClickListener{
            val user_data = StompChatDTO(

            )
            mAdapter.addItem(user_data)
            mAdapter.notifyDataSetChanged()
            binding.messageActivityEditText.setText("")
        }

    }

    override fun onStart() {
        super.onStart()
        Intent(this, StompClientService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_EXTERNAL_SERVICE)
        }
    }
}