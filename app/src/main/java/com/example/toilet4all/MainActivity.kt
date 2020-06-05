package com.example.toilet4all

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.gms.location.*
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.*
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.overlay.Overlay
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.util.MarkerIcons
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONException
import org.json.JSONObject
import java.util.concurrent.Executors
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var dbHelper: ToiletDBHelper
    var backKeyPressedTime: Long = 0
    val executor = Executors.newSingleThreadExecutor()
    val handler = Handler(Looper.getMainLooper())
    lateinit var naverMap: NaverMap
    lateinit var loc: LatLng
    lateinit var fusedLocationClient: FusedLocationProviderClient
    lateinit var locationCallback: LocationCallback
    lateinit var locationRequest: LocationRequest
    lateinit var locationSource: FusedLocationSource

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)
        initMap()
    }

    private fun initLocation() {
        if (ActivityCompat.checkSelfPermission(this,
            Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient?.lastLocation?.addOnSuccessListener {
                loc = LatLng(it.latitude, it.longitude)
            }
            startLocationUpdates()
        }
        else {
            ActivityCompat.requestPermissions(this,
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE)
        }
    }

    private fun startLocationUpdates() {
        locationRequest = LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        locationCallback = object: LocationCallback() {
            override fun onLocationResult(p0: LocationResult?) {
                p0 ?: return
                for (location in p0.locations) {
                    loc = LatLng(location.latitude, location.longitude)
                    naverMap.locationOverlay.position = loc
                }
            }
        }

        if (ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
            fusedLocationClient?.lastLocation?.addOnSuccessListener {
                loc = LatLng(it.latitude, it.longitude)
            }
            fusedLocationClient.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )

        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    override fun onPause() {
        super.onPause()
        stopLocationUpdates()
    }

    private fun initMap() {
        val fm = supportFragmentManager
        val mapFragment = fm.findFragmentById(R.id.map) as MapFragment?
            ?: MapFragment.newInstance().also {
                fm.beginTransaction().add(R.id.map, it).commit()
            }
        mapFragment.getMapAsync(this)
    }

    private fun parseJson(markers: MutableList<Marker>,  toilets: MutableList<Toilet>) {
        val assetManager = resources.assets
        val inputStream = assetManager.open("nationalpublictoiletstandarddata.json")
        val jsonStr = inputStream.bufferedReader().use { it.readText() }

        val jsonObject = JSONObject(jsonStr)
        val jsonArr = jsonObject.getJSONArray("records")    // 32136
        Log.d("json size", jsonArr.length().toString())


        for (i in 0 until jsonArr.length()) {
            val obj = jsonArr.getJSONObject(i)
            val tid = i + 1
            val toiletType = obj.getString("구분")
            val toiletNm = obj.getString("화장실명")
            var rdnmadr by Delegates.notNull<String>()
            try {
                rdnmadr = obj.getString("소재지도로명주소")
            }
            catch (e: JSONException) {
                rdnmadr = ""
            }
            var lnmdar by Delegates.notNull<String>()
            try {
                lnmdar = obj.getString("소재지지번주소")
            }
            catch (e: JSONException) {
                lnmdar = ""
            }
            val unisexToiletYn = obj.getString("남녀공용화장실여부")
            var menToiletBowlNumber by Delegates.notNull<Int>()
            try {
                menToiletBowlNumber = obj.getString("남성용-대변기수").toInt()
            }
            catch (e: JSONException) {
                menToiletBowlNumber = 0
            }
            val menUrineNumber = obj.getString("남성용-소변기수").toInt()
            val menHandicapToiletBowlNumber = obj.getString("남성용-장애인용대변기수").toInt()
            val menHandicapUrinalNumber = obj.getString("남성용-장애인용소변기수").toInt()
            var menChildrenToiletBottleNumber by Delegates.notNull<Int>()
            try {
                menChildrenToiletBottleNumber = obj.getString("남성용-어린이용대변기수").toInt()
            }
            catch (e: NumberFormatException) {
                menChildrenToiletBottleNumber = 0
            }
            var menChildrenUrinalNumber by Delegates.notNull<Int>()
            try {
                menChildrenUrinalNumber = obj.getString("남성용-어린이용소변기수").toInt()
            }
            catch (e: NumberFormatException) {
                menChildrenUrinalNumber = 0
            }
            val ladiesToiletBowlNumber = obj.getString("여성용-대변기수").toInt()
            val ladiesHandicapToiletBowlNumber = obj.getString("여성용-장애인용대변기수").toInt()
            val ladiesChildrenToiletBowlNumber = obj.getString("여성용-어린이용대변기수").toInt()
            val institutionNm = obj.getString("관리기관명")
            val phoneNumber = ""    // obj.getString("전화번호")
            val openTime = obj.getString("개방시간")
            val installationYear = "" // obj.getString("설치년도")
            var latitude by Delegates.notNull<Double>()
            try {
                latitude = obj.getString("위도").toDouble()
            }
            catch (e: JSONException) {
                latitude = 0.0
            }
            var hardness by Delegates.notNull<Double>()
            try {
                hardness = obj.getString("경도").toDouble()
            }
            catch (e: JSONException) {
                hardness = 0.0
            }
            val referenceData = obj.getString("데이터기준일자")

            markers += Marker().apply {
                position = LatLng(latitude, hardness)
                icon = MarkerIcons.BLACK
                iconTintColor = Color.GREEN
                captionText = toiletNm
                alpha = 0.8f
                width = Marker.SIZE_AUTO
                height = Marker.SIZE_AUTO
                isHideCollidedSymbols = true
                isHideCollidedMarkers = true
            }

            toilets += Toilet(
                tid,
                toiletType,
                toiletNm,
                rdnmadr,
                lnmdar,
                unisexToiletYn,
                menToiletBowlNumber,
                menUrineNumber,
                menHandicapToiletBowlNumber,
                menHandicapUrinalNumber,
                menChildrenToiletBottleNumber,
                menChildrenUrinalNumber,
                ladiesToiletBowlNumber,
                ladiesHandicapToiletBowlNumber,
                ladiesChildrenToiletBowlNumber,
                institutionNm,
                phoneNumber,
                openTime,
                installationYear,
                latitude,
                hardness,
                referenceData
            )
        }
    }

    private fun initDB(naverMap: NaverMap, infoWindow: InfoWindow) {
        dbHelper = ToiletDBHelper(this)

        // db에 전국공중화장실표준데이터를 asset에 저장한다.
        // json 파일을 파싱하여 toilet 객체를 만들고 db에 넣어줌.
        if (dbHelper.getCount() > 0) {
            // db에 데이터가 이미 있으면 맵에 뿌려줘야함.
            val markers = dbHelper.getAllToilet(naverMap, infoWindow)

            handler.post {
                markers.forEach { marker ->
                    marker.map = naverMap
                    marker.onClickListener = Overlay.OnClickListener { p0 ->
                        val marker = p0 as Marker

                        if (marker.infoWindow == null)
                            infoWindow.open(marker)
                        else
                            infoWindow.close()

                        true
                    }
                }
            }
            return
        }

        Toast.makeText(this, "데이터를 불러오는 중입니다.", Toast.LENGTH_SHORT).show()
        // 백그라운드에서 marker를 생성해야 함.
        executor.execute {
            val markers = mutableListOf<Marker>()
            val toilets = mutableListOf<Toilet>()
            parseJson(markers, toilets)

            handler.post {
                markers.forEach { marker ->
                    marker.map = naverMap
                    marker.onClickListener = Overlay.OnClickListener { p0 ->
                        val marker = p0 as Marker

                        if (marker.infoWindow == null)
                            infoWindow.open(marker)
                        else
                            infoWindow.close()

                        true
                    }
                }
                Log.d("toilet list size", toilets.size.toString())
                dbHelper.insertAllToilet(toilets)
            }
        }
    }

    private fun init() {


        drawerLayout.addDrawerListener(object: DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {}
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerClosed(drawerView: View) {}
            override fun onDrawerOpened(drawerView: View) {}
        })

        settingBtn.setOnClickListener {
            drawerLayout.openDrawer(Gravity.LEFT)
        }

        // boardActivity에 intent해야 함.
        boardBtn.setOnClickListener {
            val intent = Intent(this, BoardActivity::class.java)
            startActivity(intent)
        }

    }

    override fun onBackPressed() {
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis()
            Toast.makeText(this, "한 번 더 누르면 앱을 종료합니다.", Toast.LENGTH_SHORT).show()
            return
        }
        else
            finish()
    }

    override fun onMapReady(p0: NaverMap) {
        naverMap = p0
        naverMap.locationSource = locationSource
        val lat = locationSource.lastLocation?.latitude
        val lng = locationSource.lastLocation?.longitude
        if (lat != null && lng != null)
            naverMap.moveCamera(CameraUpdate.scrollTo(LatLng(lat, lng)))
        Log.d("현재위치", lat.toString() + ", " + lng.toString())
        naverMap.mapType = NaverMap.MapType.Basic
        naverMap.maxZoom = 16.0
        naverMap.minZoom = 12.0
        naverMap.uiSettings.isLocationButtonEnabled = true
        naverMap.uiSettings.isCompassEnabled = true
        naverMap.uiSettings.isScaleBarEnabled = true
        naverMap.uiSettings.isCompassEnabled = true
        naverMap.uiSettings.isScrollGesturesEnabled = true
        naverMap.uiSettings.isRotateGesturesEnabled = true
        naverMap.locationOverlay.isVisible = true
        naverMap.locationOverlay.circleRadius = 400
        naverMap.locationTrackingMode = LocationTrackingMode.Follow

        val infoWindow = InfoWindow()
        infoWindow.adapter = object: InfoWindow.DefaultTextAdapter(this) {
            override fun getText(p0: InfoWindow): CharSequence {
                return infoWindow.marker!!.captionText
            }
        }

        naverMap.setOnMapClickListener { point, coord  ->
            infoWindow.close()
        }

        initLocation()
        initDB(naverMap, infoWindow)
        init()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (locationSource.onRequestPermissionsResult(requestCode, permissions,
            grantResults)) {
            if (!locationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.Follow
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}
