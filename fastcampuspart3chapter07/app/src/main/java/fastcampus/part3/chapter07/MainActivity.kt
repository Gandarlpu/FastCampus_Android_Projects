package fastcampus.part3.chapter07

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import androidx.viewpager2.widget.ViewPager2
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

class MainActivity : AppCompatActivity() , OnMapReadyCallback{

    private lateinit var locationSource : FusedLocationSource
    private lateinit var naverMap : NaverMap
    private val mapView : MapView by lazy {
        findViewById(R.id.mapView)
    }
    private val viewPager : ViewPager2 by lazy {
        findViewById(R.id.houseViewPager)
    }
    private val viewPagerAdapter = HouseViewPagerAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // 네이버 지도 인증정보
        mapView.onCreate(savedInstanceState)

        // 네이버 지도 객체 얻어오기 위해선 OnMapReadyCallback 함수가 잇어야함.
        // 그래서 MainActivity에서 implement로 구현하고
        // 따라서 MainActivity가 OnMapReadyCallback의 구현체가 됨 = this로 callback가능
        mapView.getMapAsync(this)

        viewPager.adapter = viewPagerAdapter

        // ViewPager클릭 이벤트
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                val selectedHouseModel = viewPagerAdapter.currentList[position]
                val cameraUpdate = CameraUpdate.scrollTo(LatLng(selectedHouseModel.lat , selectedHouseModel.lng))
                    .animate(CameraAnimation.Easing)

                naverMap.moveCamera(cameraUpdate)
            }
        })
    }

    override fun onMapReady(map : NaverMap) {
        // Map조작 가능 함수
        naverMap = map

        naverMap.maxZoom = 18.0
        naverMap.minZoom = 10.0

        val cameraUpdate = CameraUpdate.scrollTo(LatLng(37.0 , 127.0)) // 위도경도
        naverMap.moveCamera(cameraUpdate)

        // 현 위치 얻어오기
        // 위치 정보는 민감한 정보이기 때문이 사용자 동의를 받아야함.
        val uiSetting = naverMap.uiSettings
        uiSetting.isLocationButtonEnabled = true

        locationSource = FusedLocationSource(this@MainActivity , LOCATION_PERMISSION_REQUEST_CODE)
        naverMap.locationSource = locationSource

        // Naver Map API안내 / 개발가이드 / 더 많은 메소드 존재함.

        getHoustListFromAPI()
        // onCrate가 되면 네이버맵가져오고 onMapReady호출 후 지도가 다 그려진 이후에 API를 호출해서 데이터를 가져온다음
        // 데이터 다 가져온 이후 마커를 찍음.
    }

    private fun getHoustListFromAPI() {
        val retrofit = Retrofit.Builder()
            .baseUrl("https://run.mocky.io")
            .addConverterFactory(GsonConverterFactory.create())
            .build()

        //호출
        retrofit.create(HouseService::class.java).also {
            it.getHouseList()
                .enqueue(object : Callback<HouseDTO>{
                    override fun onResponse(call: Call<HouseDTO>, response: Response<HouseDTO>) {
                        if(response.isSuccessful.not()){
                            // 실패 처리에 대한 구현
                            return
                        }

                        response.body()?.let { dto ->
                            Log.d("Retrofit" , dto.toString())
                        }
                    }

                    override fun onFailure(call: Call<HouseDTO>, t: Throwable) {
                        // 실패 처리에 대한 구현
                        Log.d("Retrofit" , "실패")
                    }
                })
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode != LOCATION_PERMISSION_REQUEST_CODE){
            return
        }

        if(locationSource.onRequestPermissionsResult(requestCode , permissions , grantResults)){
            if(!locationSource.isActivated){
                naverMap.locationTrackingMode = LocationTrackingMode.None
            }
            return
        }
    }

    // Activity에서 MapView 사용시 액티비티 생명주기 직접 넘겨줘야 함.
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

}