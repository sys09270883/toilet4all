package com.example.toilet4all

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import android.graphics.Color
import android.util.Log
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.MarkerIcons

class ToiletDBHelper(context: Context) : SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {

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
        val createTable= "create table if not exists " + TABLE_NAME + "(" +
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
        db?.execSQL(createTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        val dropTable = "drop table if exists $TABLE_NAME"
        db?.execSQL(dropTable)
        onCreate(db)
    }

    fun findToilet(toiletNm: String): Toilet? {
        val db = this.readableDatabase
        val findToilet = "select * from " + TABLE_NAME +
                " where $TOILET_NAME = '$toiletNm'"
        val cursor = db.rawQuery(findToilet, null)
        if (cursor.count != 0) {
            cursor.moveToFirst()
            val toilet = Toilet(
                cursor.getInt(0),
                cursor.getString(1),
                cursor.getString(2),
                cursor.getString(3),
                cursor.getString(4),
                cursor.getString(5),
                cursor.getInt(6),
                cursor.getInt(7),
                cursor.getInt(8),
                cursor.getInt(9),
                cursor.getInt(10),
                cursor.getInt(11),
                cursor.getInt(12),
                cursor.getInt(13),
                cursor.getInt(14),
                cursor.getString(15),
                cursor.getString(16),
                cursor.getString(17),
                cursor.getString(18),
                cursor.getDouble(19),
                cursor.getDouble(20),
                cursor.getString(21)
            )
            cursor.close()
            db.close()
            return toilet
        }
        return null
    }

    fun insertAllToilet(toilets: MutableList<Toilet>) {
        /* total 29468 toilets */
        val db = this.writableDatabase
        db.beginTransaction()
        toilets.forEach { toilet ->
            if (toilet.latitude > 0.0 && toilet.hardness > 0.0) {
                val toiletValues = ContentValues()
                toiletValues.put(TOILET_TYPE, toilet.toiletType)
                toiletValues.put(TOILET_NAME, toilet.tolietNm)
                toiletValues.put(RDNMADR, toilet.rdnmadr)
                toiletValues.put(LNMDAR, toilet.lnmdar)
                toiletValues.put(UNISEX_TOILET_YN, toilet.unisexToiletYn)
                toiletValues.put(MEN_TOILET_BOWL_NUMBER, toilet.menToiletBowlNumber)
                toiletValues.put(MEN_URINE_NUMBER, toilet.menHandicapUrinalNumber)
                toiletValues.put(MEN_HANDICAP_TOILET_BOWL_NUMBER, toilet.menHandicapToiletBowlNumber)
                toiletValues.put(MEN_HANDICAP_URINAL_NUMBER, toilet.menHandicapUrinalNumber)
                toiletValues.put(MEN_CHILDREN_TOILET_BOTTLE_NUMBER, toilet.menChildrenToiletBottleNumber)
                toiletValues.put(MEN_CHILDREN_URINAL_NUMBER, toilet.menChildrenUrinalNumber)
                toiletValues.put(LADIES_TOILET_BOWL_NUMBER, toilet.ladiesToiletBowlNumber)
                toiletValues.put(LADIES_HANDICAP_TOILET_BOWL_NUMBER, toilet.ladiesHandicapToiletBowlNumber)
                toiletValues.put(LADIES_CHILDREN_TOILET_BOWL_NUMBER, toilet.ladiesChildrenToiletBowlNumber)
                toiletValues.put(INSTITUTION_NAME, toilet.institutionNm)
                toiletValues.put(PHONE_NUMBER, toilet.phoneNumber)
                toiletValues.put(OPEN_TIME, toilet.openTime)
                toiletValues.put(INSTALLATION_YEAR, toilet.installationYear)
                toiletValues.put(LATITUDE, toilet.latitude)
                toiletValues.put(HARDNESS, toilet.hardness)
                toiletValues.put(REFERENCE_DATA, toilet.referenceData)
                db.insert(TABLE_NAME, null, toiletValues)
            }
        }
        db.setTransactionSuccessful()
        db.endTransaction()
    }

    fun getAllToilet(markers: ArrayList<Marker>) {
        val getAllToilet = "select * from " + TABLE_NAME
        val db = this.readableDatabase
        val cursor = db.rawQuery(getAllToilet, null)
        if (cursor.count > 0)
            setMarker(cursor, markers)
        cursor.close()
        db.close()
    }

    private fun setMarker(cursor: Cursor, markers: ArrayList<Marker>) {
        synchronized(this) {
            cursor.moveToFirst()
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
    }

    fun getCount(): Int {
        val db = this.readableDatabase
        val cursor = db.rawQuery("select * from $TABLE_NAME", null)
        return cursor.count
    }

    fun getOptionMarkers(markers: ArrayList<Marker>, options: Int) {
        markers.clear()
        val db = this.readableDatabase
        var query = "select * from $TABLE_NAME"

        if ((options and 0b1000) == 0b1000)
            query += " where $UNISEX_TOILET_YN = 'Y'"
        if ((options and 0b0100) == 0b0100) {
            query += if ((options shr 3) > 0)
                " and "
            else
                " where "
            query += "$UNISEX_TOILET_YN = 'N'"
        }
        if ((options and 0b0010) == 0b0010) {
            query += if ((options shr 2) > 0)
                " and "
            else
                " where "
            query += "($MEN_HANDICAP_TOILET_BOWL_NUMBER > 0 or $MEN_HANDICAP_URINAL_NUMBER > 0 or " +
                    "$LADIES_HANDICAP_TOILET_BOWL_NUMBER > 0)"
        }
        if ((options and 0b0001) == 0b0001) {
            query += if ((options shr 1) > 0)
                " and "
            else
                " where "
            query += "($MEN_CHILDREN_URINAL_NUMBER > 0 or $MEN_CHILDREN_TOILET_BOTTLE_NUMBER > 0 or " +
                    "$LADIES_CHILDREN_TOILET_BOWL_NUMBER > 0)"
        }

        Log.d("options", options.toString())
        Log.d("query", query)
        val cursor = db.rawQuery(query, null)
        if (cursor.count > 0)
            setMarker(cursor, markers)

        Log.d("size", markers.size.toString())
        cursor.close()
        db.close()
    }

}

