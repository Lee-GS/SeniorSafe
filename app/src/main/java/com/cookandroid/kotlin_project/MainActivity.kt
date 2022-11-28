package com.cookandroid.kotlin_project

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.appcompat.app.AlertDialog
import androidx.room.Room
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.cookandroid.kotlin_project.backendinterface.auth.signin
import com.cookandroid.kotlin_project.backendinterface.dto.UserDTO
import com.cookandroid.kotlin_project.databinding.ActivityMainBinding
import com.cookandroid.kotlin_project.localDB.UserEntity
import com.cookandroid.kotlin_project.localDB.database.UserDatabase
import com.cookandroid.kotlin_project.stomp.StompClientService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : AppCompatActivity() {

    private val api_singin = signin.create();
    private lateinit var roomDB : UserDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent: Intent = Intent(this, JoinActivity::class.java)//intent 선언
        val intent2: Intent = Intent(this, MainActivity_maps::class.java)
        val intent3: Intent = Intent(this, StompClientService::class.java)
        val binding = ActivityMainBinding.inflate(layoutInflater)// java의 findviewbyid 작업을 안해도됨

        setContentView(binding.root)

        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(database: SupportSQLiteDatabase) {
                database.execSQL("DROP TABLE UserTable")
            }
        }
        roomDB = Room.databaseBuilder(
            applicationContext,
            UserDatabase::class.java,
            "UserTable"
        ).fallbackToDestructiveMigration().addMigrations(MIGRATION_1_2).build()

        binding.btnJoin.setOnClickListener {
            startActivity(intent)
        }
        binding.btnLogin.setOnClickListener{
            val data_signin = UserDTO(
                email = binding.email.text.toString(),
                password = binding.PW.text.toString(),
            )
            var dialog = AlertDialog.Builder(this@MainActivity)
            api_singin.register_signin(data_signin).enqueue(object : Callback<UserDTO> {
                override fun onResponse(call: Call<UserDTO>, response: Response<UserDTO>) {
                    val result = response.code();
                    if(result in 200..299) {
                        Log.d("로그인성공", response.body().toString())
                        intent3.putExtra("token_login", response.body()!!.token)

                        val userDTO = response.body()!!
                        var userEntity = UserEntity(
                            uid = 0,
                            email = userDTO.email,
                            username = userDTO.username,
                            realname = userDTO.realname)

                        Log.d("DEBUG", userEntity.uid.toString())

                        Thread {
                            val dao = roomDB.userDao()
                            dao.insertAll(userEntity)
                            val userEntity = dao.getAll()
                            Log.d("DEBUG", userEntity.toString())
                        }.start()

                        stopService(intent3)
                        startService(intent3)
                        startActivity(intent2)
                    }
                    else {
                        Log.w("로그인실패", response.body().toString())
                        dialog.setTitle("에러")
                        dialog.setMessage("로그인에 실패하셨습니다.")
                        dialog.setPositiveButton(
                            "확인",
                            DialogInterface.OnClickListener { dlg, id -> "확인" })
                        dialog.show()
                    }
                }

                override fun onFailure(call: Call<UserDTO>, t: Throwable) {
                    Log.e("연결 실패","${t.localizedMessage}")
                    t.printStackTrace()
                    dialog.setTitle("에러")
                    dialog.setMessage("로그인에 실패하셨습니다.")
                    dialog.show()

                }
            })
        }

    }
}