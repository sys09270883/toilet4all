package com.example.toilet4all

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Color
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.NaverMap
import com.naver.maps.map.overlay.InfoWindow
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.MarkerIcons

class ToiletDBHelper(var context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

    companion object {
        val DB_VERSION = 1
        val DB_NAME = "toiletdb.db"
        val TABLE_NAME = "toilets"
        val TOILET_ID = "tid"
        val TOILET_TYPE = "toiletType"
        val TOILET_NAME = "toiletNm"
        val RDNMADR = "rdnmadr"
        val LNMDAR = "lnmdar"
        val UNISEX_TOILET_YN = "unisexToiletYn"
        val MEN_TOILET_BOWL_NUMBER = "menToiletBowlNumber"
        val MEN_URINE_NUMBER = "menUrineNumber"
        val MEN_HANDICAP_TOILET_BOWL_NUMBER = "menHandicapToiletBowlNumber"
        val MEN_HANDICAP_URINAL_NUMBER = "menHandicapUrinalNumber"
        val MEN_CHILDREN_TOILET_BOTTLE_NUMBER = "menChildrenToiletBottleNumber"
        val MEN_CHILDREN_URINAL_NUMBER = "menChildrenUrinalNumber"
        val LADIES_TOILET_BOWL_NUMBER = "ladiesToiletBowlNumber"
        val LADIES_HANDICAP_TOILET_BOWL_NUMBER = "ladiesHandicapToiletBowlNumber"
        val LADIES_CHILDREN_TOILET_BOWL_NUMBER = "ladiesChildrenToiletBowlNumber"
        val INSTITUTION_NAME = "institutionNm"
        val PHONE_NUMBER = "phoneNumber"
        val OPEN_TIME = "openTime"
        val INSTALLATION_YEAR = "installationYear"
        val LATITUDE = "latitude"
        val HARDNESS = "hardness"
        val REFERENCE_DATA = "referenceData"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val create_table= "create table if not exists " + TABLE_NAME + "(" +
                TOILET_ID + " integer primary key autoincrement, " +
                TOILET_TYPE + " varchar(50), " +
                TOILET_NAME + " varchar(30), " +
                RDNMADR + " varchar(50), " +
                LNMDAR + " varchar(50), " +
                UNISEX_TOILET_YN + " varchar(2), " +
                MEN_TOILET_BOWL_NUMBER + " integer, " +
                MEN_URINE_NUMBER + " integer, " +
                MEN_HANDICAP_TOILET_BOWL_NUMBER + " integer, " +
                MEN_HANDICAP_URINAL_NUMBER + " integer, " +
                MEN_CHILDREN_TOILET_BOTTLE_NUMBER + " integer, " +
                MEN_CHILDREN_URINAL_NUMBER + " integer, " +
                LADIES_TOILET_BOWL_NUMBER + " integer, " +
                LADIES_HANDICAP_TOILET_BOWL_NUMBER + " integer, " +
                LADIES_CHILDREN_TOILET_BOWL_NUMBER + " integer, " +
                INSTITUTION_NAME + " varchar(20), " +
                PHONE_NUMBER + " varchar(14), " +
                OPEN_TIME + " varchar(20), " +
                INSTALLATION_YEAR + " varchar(20), " +
                LATITUDE + " real, " +
                HARDNESS + " real, " +
                REFERENCE_DATA + " varchar(30))"
        db?.execSQL(create_table)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val drop_table = "drop table if exists " + TABLE_NAME
        db?.execSQL(drop_table)
        onCreate(db)
    }

    fun insertAllToilet(toilets: MutableList<Toilet>) {
        this.writableDatabase.beginTransaction()
        toilets.forEach { toilet ->
            if (toilet.latitude > 1.0 && toilet.hardness > 1.0) {
                val toiletValues = ContentValues()
                toiletValues.put(ToiletDBHelper.TOILET_TYPE, toilet.toiletType)
                toiletValues.put(ToiletDBHelper.TOILET_NAME, toilet.tolietNm)
                toiletValues.put(ToiletDBHelper.RDNMADR, toilet.rdnmadr)
                toiletValues.put(ToiletDBHelper.LNMDAR, toilet.lnmdar)
                toiletValues.put(ToiletDBHelper.UNISEX_TOILET_YN, toilet.unisexToiletYn)
                toiletValues.put(ToiletDBHelper.MEN_TOILET_BOWL_NUMBER, toilet.menToiletBowlNumber)
                toiletValues.put(ToiletDBHelper.MEN_URINE_NUMBER, toilet.menHandicapUrinalNumber)
                toiletValues.put(ToiletDBHelper.MEN_HANDICAP_TOILET_BOWL_NUMBER, toilet.menHandicapToiletBowlNumber)
                toiletValues.put(ToiletDBHelper.MEN_HANDICAP_URINAL_NUMBER, toilet.menHandicapUrinalNumber)
                toiletValues.put(ToiletDBHelper.MEN_CHILDREN_TOILET_BOTTLE_NUMBER, toilet.menChildrenToiletBottleNumber)
                toiletValues.put(ToiletDBHelper.MEN_CHILDREN_URINAL_NUMBER, toilet.menChildrenUrinalNumber)
                toiletValues.put(ToiletDBHelper.LADIES_TOILET_BOWL_NUMBER, toilet.ladiesToiletBowlNumber)
                toiletValues.put(ToiletDBHelper.LADIES_HANDICAP_TOILET_BOWL_NUMBER, toilet.ladiesHandicapToiletBowlNumber)
                toiletValues.put(ToiletDBHelper.LADIES_CHILDREN_TOILET_BOWL_NUMBER, toilet.ladiesChildrenToiletBowlNumber)
                toiletValues.put(ToiletDBHelper.INSTITUTION_NAME, toilet.institutionNm)
                toiletValues.put(ToiletDBHelper.PHONE_NUMBER, toilet.phoneNumber)
                toiletValues.put(ToiletDBHelper.OPEN_TIME, toilet.openTime)
                toiletValues.put(ToiletDBHelper.INSTALLATION_YEAR, toilet.installationYear)
                toiletValues.put(ToiletDBHelper.LATITUDE, toilet.latitude)
                toiletValues.put(ToiletDBHelper.HARDNESS, toilet.hardness)
                toiletValues.put(ToiletDBHelper.REFERENCE_DATA, toilet.referenceData)
                this.writableDatabase.insert(ToiletDBHelper.TABLE_NAME, null, toiletValues)
            }
        }
        this.writableDatabase.setTransactionSuccessful()
        this.writableDatabase.endTransaction()
    }

    fun getAllToilet(naverMap: NaverMap, infoWindow: InfoWindow): ArrayList<Marker> {
        val markers = ArrayList<Marker>()
        val getAllToilet = "select * from " + TABLE_NAME
        val db = this.readableDatabase
        val cursor = db.rawQuery(getAllToilet, null)
        if (cursor.count > 0) {
            setMarker(naverMap, infoWindow, cursor, markers)
        }
        cursor.close()
        db.close()
        return markers
    }

    fun setMarker(naverMap: NaverMap, infoWindow: InfoWindow, cursor: Cursor, markers: ArrayList<Marker>) {
        cursor.moveToFirst()
        // 마커 예제: 공공데이터를 받아와서 마커를 표시하면 될듯 함.

        do {
            val lat = cursor.getString(19).toDouble()   // 위도
            val lng = cursor.getString(20).toDouble()   // 경도
            val toiletName = cursor.getString(2)

            markers += Marker().apply {
                position = LatLng(lat, lng)
                icon = MarkerIcons.BLACK
                iconTintColor = Color.GREEN
                captionText = toiletName
                alpha = 0.8f
                width = Marker.SIZE_AUTO
                height = Marker.SIZE_AUTO
                isHideCollidedSymbols = true
                isHideCollidedMarkers = true
            }
        } while (cursor.moveToNext())
    }

    fun getCount(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("select * from " + TABLE_NAME, null)
        return cursor.count
    }

}

