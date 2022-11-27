package com.cookandroid.kotlin_project

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.IBinder
import android.os.Looper
import android.util.Log
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.annotation.UiThread
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.FragmentActivity
import com.cookandroid.kotlin_project.BuildConfig.api_key
import com.cookandroid.kotlin_project.stomp.StompClientService
import com.google.android.gms.location.*
import com.google.android.material.navigation.NavigationView
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import io.reactivex.disposables.Disposable
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.net.Socket
import java.util.concurrent.TimeUnit
import java.util.logging.Level
import java.util.logging.Logger
import kotlin.concurrent.thread



class MainActivity_maps : AppCompatActivity(), OnMapReadyCallback{

    lateinit var drawerLayout: DrawerLayout

    var TAG:String = "로그"
    val client = OkHttpClient()
    val intervalMillis = 1000L
    private lateinit var locationSource: FusedLocationSource
    private lateinit var naverMap: NaverMap

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
        setContentView(R.layout.activity_main_maps)

        val intent2: Intent = Intent(this, ChatApplication::class.java)//intent 선언

        val toolbar: Toolbar =findViewById(R.id.toolbar)
        val navigationView: NavigationView = findViewById(R.id.navigationView)
        drawerLayout = findViewById(R.id.drawerLayout)

        //액션바에 toolbar 셋팅
        setSupportActionBar(toolbar)

        //액션바 생성
        val actionBar: androidx.appcompat.app.ActionBar? = supportActionBar

        //뒤로가기 버튼 생성
        actionBar?.setDisplayHomeAsUpEnabled(true)

        //뒤로가기 버튼 이미지 변경
        actionBar?.setHomeAsUpIndicator(R.drawable.ic_menu)

        //네비게이션뷰 아이템 선택 이벤트
        navigationView.setNavigationItemSelectedListener(
            object : NavigationView.OnNavigationItemSelectedListener{
                override fun onNavigationItemSelected(item: MenuItem): Boolean {
                    when(item.itemId){
                        R.id.senior_home -> {
                            item.isChecked = true
                            return true
                        }

                        R.id.nav_gallery -> {
                            item.isChecked = true
                            startActivity(intent2)
                            drawerLayout.closeDrawers()
                            return true
                        }

                        R.id.nav_slideshow -> {
                            item.isChecked = true
                            displayMessage("selected slideshow")
                            drawerLayout.closeDrawers()
                            return true
                        }
                        else -> {
                            return true
                        }
                    } //when
                } //onNavigationItemSelected
            } //NavigationView.OnNavigationItemSelectedListener
        ) //setNavigationItemSelectedListener


        NaverMapSdk.getInstance(this).client =
            NaverMapSdk.NaverCloudPlatformClient(api_key)

        // 뷰 역할을 하는 프래그먼트 객체 얻기
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }

        // 인터페이스 역할을 하는 NaverMap 객체 얻기
        // 프래그먼트(MapFragment)의 getMapAsync() 메서드로 OnMapReadyCallback 을 등록하면 비동기로 NaverMap 객체를 얻을 수 있다고 한다.
        // NaverMap 객체가 준비되면 OnMapReady() 콜백 메서드 호출
        mapFragment.getMapAsync(this)

        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.menu_option,menu)
        return true
    }

    private fun displayMessage(message:String){
        Toast.makeText(applicationContext, message, Toast.LENGTH_SHORT).show()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.condition -> {
                Log.d("DEBUG", "condition")
                return true
            }
            android.R.id.home -> {
                //drawerLayout 펼치기
                drawerLayout.openDrawer(GravityCompat.START)
                return true
            }

            R.id.mypage -> {
                var intent=Intent(this,MypageActivity::class.java)
                startActivity(intent)
                return true
            }

            R.id.location -> {
                Log.d("DEBUG", "location")

                naverMap.addOnLocationChangeListener { location ->
                    Log.d("GPS", "${location.latitude}, ${location.longitude}")
                    Log.d("GPS", "StompStatus:$mStompServiceBound")

                    if(mStompServiceBound)
                        stompService.sendGpsPos(location.latitude, location.longitude)
                }

                return true
            }

            R.id.logout -> {
                Toast.makeText(this, "로그아웃", Toast.LENGTH_SHORT).show()
                MySharedPreferences.clearUser(this)
                var intent=Intent(this,MainActivity::class.java)
                startActivity(intent)
                finish()

                return true
            }
            R.id.group -> {
                Log.d("DEBUG", "group")
                return true
            }
            else ->  return super.onOptionsItemSelected(item)
        }
    }

    override fun onMapReady(naverMap: NaverMap) {
        Log.d(TAG, "MainActivity - onMapReady")
        this.naverMap = naverMap
        naverMap.locationSource = locationSource
        naverMap.uiSettings.isLocationButtonEnabled = true

        Intent(this, StompClientService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_EXTERNAL_SERVICE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int,
                                            permissions: Array<String>,
                                            grantResults: IntArray) {
        Log.d(TAG, "MainActivity - onRequestPermissionsResult")
        if (locationSource.onRequestPermissionsResult(requestCode, permissions,
                grantResults)) {
            if (!locationSource.isActivated) { // 권한 거부됨
                Log.d(TAG, "MainActivity - onRequestPermissionsResult 권한 거부됨")
                naverMap.locationTrackingMode = LocationTrackingMode.None
            } else {
                Log.d(TAG, "MainActivity - onRequestPermissionsResult 권한 승인됨")
                naverMap.locationTrackingMode = LocationTrackingMode.Follow // 현위치 버튼 컨트롤 활성
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
        private const val API_KEY = BuildConfig.API_KEY
    }

}