package com.example.toilet4all

import android.Manifest
import android.annotation.SuppressLint
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.AsyncTask
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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
import kotlinx.android.synthetic.main.drawer_layout.*
import kotlinx.android.synthetic.main.information_window_layout.view.*
import org.json.JSONException
import org.json.JSONObject
import java.lang.ref.WeakReference
import kotlin.properties.Delegates

class MainActivity : AppCompatActivity(), OnMapReadyCallback {

    lateinit var dbHelper: ToiletDBHelper
    private var backKeyPressedTime: Long = 0
    val handler = Handler(Looper.getMainLooper())
    var naverMap: NaverMap ?= null
    lateinit var infoWindow: InfoWindow
    lateinit var loc: LatLng
    private var fusedLocationClient: FusedLocationProviderClient ?= null
    private lateinit var locationCallback: LocationCallback
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationSource: FusedLocationSource
    private lateinit var progressDialog: AlertDialog
    var markers = ArrayList<Marker>()
    var lastOptions = 0

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    private fun startTask(options: Int, isFirst: Boolean = false) {
        val task = ToiletAsyncTask(this, options, isFirst)
        task.execute()
    }

    class ToiletAsyncTask(context: MainActivity,
                          private val options: Int,
                          private val isFirst: Boolean = false)
        : AsyncTask<Unit, String, Unit>() {
        private val activityReference = WeakReference(context)

        override fun doInBackground(vararg params: Unit?) {
            val activity = activityReference.get()!!
            var tidList = ArrayList<Int>()

            if (isFirst) {
                val toilets = ArrayList<Toilet>()
                activity.parseJson(toilets)
                activity.dbHelper.insertAllToilet(toilets)
            }
            else
                tidList = activity.dbHelper.getOptionMarkers(options)

            activity.handler.post {
                activity.lastOptions = options

                when (isFirst) {
                    true -> {
                        activity.markers.forEach { marker ->
                            marker.map = activity.naverMap
                            marker.onClickListener = Overlay.OnClickListener { p0 ->
                                val selectedMarker = p0 as Marker

                                if (selectedMarker.infoWindow == null)
                                    activity.infoWindow.open(selectedMarker)
                                else
                                    activity.infoWindow.close()

                                true
                            }
                        }
                    }
                    false -> {
                        for (i in tidList) {
                            activity.markers[i].apply {
                                map = activity.naverMap
                                onClickListener = Overlay.OnClickListener { p0 ->
                                    val selectedMarker = p0 as Marker

                                    if (selectedMarker.infoWindow == null)
                                        activity.infoWindow.open(selectedMarker)
                                    else
                                        activity.infoWindow.close()

                                    true
                                }
                            }
                        }
                    }
                }
            }
        }

        override fun onPreExecute() {
            val activity = activityReference.get()!!
            val builder = AlertDialog.Builder(activity)
            activity.progressDialog = builder
                .setView(R.layout.progress_bar_layout)
                .setMessage("데이터를 저장하는 중입니다.")
                .setCancelable(false)
                .create()
            activity.progressDialog.show()
            super.onPreExecute()
        }

        override fun onPostExecute(result: Unit?) {
            val activity = activityReference.get()!!
            activity.progressDialog.dismiss()
            super.onPostExecute(result)
        }

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val intent = Intent(this, LoadingActivity::class.java)
        startActivity(intent)

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
                    naverMap?.locationOverlay?.position = loc
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
            fusedLocationClient?.requestLocationUpdates(
                locationRequest,
                locationCallback,
                Looper.getMainLooper()
            )
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient?.removeLocationUpdates(locationCallback)
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

    private fun parseJson(toilets: ArrayList<Toilet>) {
        val assetManager = resources.assets
        val inputStream = assetManager.open("nationalpublictoiletstandarddata.json")
        val jsonStr = inputStream.bufferedReader().use { it.readText() }
        val jsonObject = JSONObject(jsonStr)
        val jsonArr = jsonObject.getJSONArray("records")
        var tid = 0

        for (i in 0 until jsonArr.length()) {
            val obj = jsonArr.getJSONObject(i)
            val toiletType = obj.getString("구분")
            val toiletNm = obj.getString("화장실명")
            var rdnmadr by Delegates.notNull<String>()
            rdnmadr = try {
                obj.getString("소재지도로명주소")
            } catch (e: JSONException) {
                ""
            }
            var lnmdar by Delegates.notNull<String>()
            lnmdar = try {
                obj.getString("소재지지번주소")
            } catch (e: JSONException) {
                ""
            }
            val unisexToiletYn = obj.getString("남녀공용화장실여부")
            var menToiletBowlNumber by Delegates.notNull<Int>()
            menToiletBowlNumber = try {
                obj.getString("남성용-대변기수").toInt()
            } catch (e: JSONException) {
                0
            }
            val menUrineNumber = obj.getString("남성용-소변기수").toInt()
            val menHandicapToiletBowlNumber = obj.getString("남성용-장애인용대변기수").toInt()
            val menHandicapUrinalNumber = obj.getString("남성용-장애인용소변기수").toInt()
            var menChildrenToiletBottleNumber by Delegates.notNull<Int>()
            menChildrenToiletBottleNumber = try {
                obj.getString("남성용-어린이용대변기수").toInt()
            } catch (e: NumberFormatException) {
                0
            }
            var menChildrenUrinalNumber by Delegates.notNull<Int>()
            menChildrenUrinalNumber = try {
                obj.getString("남성용-어린이용소변기수").toInt()
            } catch (e: NumberFormatException) {
                0
            }
            val ladiesToiletBowlNumber = obj.getString("여성용-대변기수").toInt()
            val ladiesHandicapToiletBowlNumber = obj.getString("여성용-장애인용대변기수").toInt()
            val ladiesChildrenToiletBowlNumber = obj.getString("여성용-어린이용대변기수").toInt()
            val institutionNm = obj.getString("관리기관명")
            val phoneNumber = ""    // obj.getString("전화번호")
            val openTime = obj.getString("개방시간")
            val installationYear = "" // obj.getString("설치년도")
            var latitude by Delegates.notNull<Double>()
            latitude = try {
                obj.getString("위도").toDouble()
            } catch (e: JSONException) {
                0.0
            }
            var hardness by Delegates.notNull<Double>()
            hardness = try {
                obj.getString("경도").toDouble()
            } catch (e: JSONException) {
                0.0
            }
            val referenceData = obj.getString("데이터기준일자")

            if (latitude > 0.0 && hardness > 0.0) {
                markers.plusAssign(Marker().apply {
                    position = LatLng(latitude, hardness)
                    icon = MarkerIcons.BLACK
                    iconTintColor = Color.GREEN
                    alpha = 0.8f
                    width = 70
                    height = 110
                    isHideCollidedSymbols = true
                    isHideCollidedMarkers = true
                    captionText = toiletNm
                })

                toilets += Toilet(
                    tid++,
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
    }

    private fun initDB() {
        dbHelper = ToiletDBHelper(this)

        /* internal database already exists */
        var cnt: Int
        synchronized(this) {
            cnt = dbHelper.getCount()
        }
        if (cnt > 0) {
            dbHelper.getOptionMarkers(0, true)

            handler.post {
                markers.forEach { marker ->
                    marker.map = naverMap
                    marker.onClickListener = Overlay.OnClickListener { p0 ->
                        val selectedMarker = p0 as Marker

                        if (selectedMarker.infoWindow == null)
                            infoWindow.open(selectedMarker)
                        else
                            infoWindow.close()

                        true
                    }
                }
            }
            return
        }
        else
            startTask(0, true)
    }

    @SuppressLint("RtlHardcoded")
    private fun init() {
        drawerLayout.addDrawerListener(object: DrawerLayout.DrawerListener {
            override fun onDrawerStateChanged(newState: Int) {}
            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
            override fun onDrawerClosed(drawerView: View) {
                var options = 0
                if (sharedToiletRadioBtn.isChecked)
                    options = options or 0b1000
                if (separatedToiletRadioBtn.isChecked)
                    options = options or 0b0100
                if (disabledToiletCheckBox.isChecked)
                    options = options or 0b0010
                if (childToiletCheckBox.isChecked)
                    options = options or 0b0001

                if (lastOptions == options)
                    return

                for (marker in markers) {
                    marker.map = null
                }

                startTask(options)
            }
            override fun onDrawerOpened(drawerView: View) {}
        })

        settingBtn.setOnClickListener {
            drawerLayout.openDrawer(Gravity.LEFT)
        }

        boardBtn.setOnClickListener {
            val intent = Intent(this, BoardActivity::class.java)
            startActivity(intent)
        }

        findBtn.setOnClickListener {
            var minDist: Double = Double.MAX_VALUE
            val curPosition = LatLng(locationSource.lastLocation!!.latitude,
                locationSource.lastLocation!!.longitude)
            var nearestPosition: LatLng = markers[0].position
            for (i in 1 until markers.size) {
                if (markers[i].map == null)
                    continue
                val dist = getDistance(curPosition, markers[i].position)
                if (minDist > dist) {
                    minDist = dist
                    nearestPosition = markers[i].position
                }
            }
            naverMap?.moveCamera(CameraUpdate.scrollTo(nearestPosition)
                .animate(CameraAnimation.Easing, 1000))
        }

    }

    private fun getDistance(p1: LatLng, p2: LatLng): Double {
        val ret = (p1.latitude - p2.latitude) * (p1.latitude - p2.latitude) +
                (p1.longitude - p2.longitude) * (p1.longitude - p2.longitude)
        return ret
    }

    @SuppressLint("RtlHardcoded")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(Gravity.LEFT)) {
            drawerLayout.closeDrawer(Gravity.LEFT)
            return
        }
        if (System.currentTimeMillis() > backKeyPressedTime + 2000) {
            backKeyPressedTime = System.currentTimeMillis()
            Toast.makeText(this, "한 번 더 누르면 앱을 종료합니다.", Toast.LENGTH_SHORT).show()
            return
        }
        else {
            markers.clear()
            moveTaskToBack(true)
            finish()
        }
    }

    override fun onMapReady(p0: NaverMap) {
        naverMap = p0
        naverMap?.locationSource = locationSource
        naverMap?.mapType = NaverMap.MapType.Basic
        naverMap?.maxZoom = 18.0
        naverMap?.minZoom = 12.0
        naverMap?.uiSettings?.isLocationButtonEnabled = true
        naverMap?.uiSettings?.isCompassEnabled = true
        naverMap?.uiSettings?.isScaleBarEnabled = true
        naverMap?.uiSettings?.isCompassEnabled = true
        naverMap?.uiSettings?.isScrollGesturesEnabled = true
        naverMap?.uiSettings?.isRotateGesturesEnabled = true
        naverMap?.locationOverlay?.isVisible = true
        naverMap?.locationOverlay?.circleRadius = 400
        naverMap?.locationTrackingMode = LocationTrackingMode.Follow

        infoWindow = InfoWindow()
        infoWindow.alpha = 0.9f
        infoWindow.adapter = object: InfoWindow.DefaultViewAdapter(this) {
            @SuppressLint("InflateParams")
            override fun getContentView(p0: InfoWindow): View {
                val view = LayoutInflater.from(this@MainActivity)
                    .inflate(R.layout.information_window_layout, null, false)
                val toilet = dbHelper.findToilet(p0.marker!!.captionText)!!
                view.infoTitleView.text = toilet.tolietNm
                view.infoSubTitleView.text = toilet.toiletType
                view.dateView.text = StringBuilder("업데이트 | " + toilet.referenceData).toString()

                if (toilet.menHandicapToiletBowlNumber + toilet.menHandicapUrinalNumber <= 0)
                    view.manHandicappedView.setColorFilter(Color.parseColor("#d3d3d3"), PorterDuff.Mode.SRC_IN)
                if (toilet.menChildrenToiletBottleNumber + toilet.menChildrenUrinalNumber <= 0)
                    view.manChildView.setColorFilter(Color.parseColor("#d3d3d3"), PorterDuff.Mode.SRC_IN)
                if (toilet.ladiesHandicapToiletBowlNumber <= 0)
                    view.womanHandicappedView.setColorFilter(Color.parseColor("#d3d3d3"), PorterDuff.Mode.SRC_IN)
                if (toilet.ladiesChildrenToiletBowlNumber <= 0)
                    view.womanChildView.setColorFilter(Color.parseColor("#d3d3d3"), PorterDuff.Mode.SRC_IN)
                if (toilet.unisexToiletYn == "Y")
                    view.toiletView.setImageResource(R.drawable.ic_shared_toilets)

                naverMap?.moveCamera(CameraUpdate.scrollTo(p0.marker!!.position)
                    .animate(CameraAnimation.Easing, 1000))
                return view
            }

        }

        naverMap?.setOnMapClickListener { _, _ ->
            infoWindow.close()
        }

        initLocation()
        initDB()
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
                naverMap?.locationTrackingMode = LocationTrackingMode.Follow
            }
            return
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}