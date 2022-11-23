package com.cookandroid.kotlin_project

import android.os.Bundle
import com.google.android.material.snackbar.Snackbar
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.cookandroid.kotlin_project.databinding.ActivityMypageBinding

class MypageActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMypageBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mypage)

     binding = ActivityMypageBinding.inflate(layoutInflater)
        setContentView(binding.root)

     binding.manager.setText("안녕하세요, ${MySharedPreferences.getUserId(this)} 님!")

     //마이페이지 이름 스낵바 클릭 이벤트
     binding.myName.setOnClickListener {
        var snackbar= Snackbar.make(binding.mypagelayout, "${MySharedPreferences.getRealName(this)}", Snackbar.LENGTH_LONG)
            snackbar.show()
        }
        //마이페이지 생년월일 스낵바 클릭 이벤트
        binding.myBirth.setOnClickListener {
            var snackbar2= Snackbar.make(binding.mypagelayout, "${MySharedPreferences.getBirthday(this)}", Snackbar.LENGTH_LONG)
            snackbar2.show()
        }
        //마이페이지 닉네임 스낵바 클릭 이벤트
        binding.myNickname.setOnClickListener {
            var snackbar3= Snackbar.make(binding.mypagelayout, "${MySharedPreferences.getUserName(this)}", Snackbar.LENGTH_LONG)
            snackbar3.show()
        }

        binding.notice.setOnClickListener {
            var snackbar4= Snackbar.make(binding.mypagelayout,"추후 개발 예정입니다.", Snackbar.LENGTH_LONG)
            snackbar4.show()
        }
        //마이페이지 앱정보 스낵바 클릭 이벤트
        binding.information.setOnClickListener {
            var snackbar5= Snackbar.make(binding.mypagelayout, "MADE BY 용시우, 조경찬, 방대혁, 이경수", Snackbar.LENGTH_LONG)
            snackbar5.show()
        }
    }


}