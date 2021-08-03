package com.example.routeplanner.database

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.example.routeplanner.database.records.PointRecord
import com.example.routeplanner.database.records.RouteRecord

class DBHelper(context: Context) : SQLiteOpenHelper(
        context, DATABASE_NAME,
        null, DATABASE_VER
) {
    companion object {
        private const val DATABASE_VER = 1
        private const val DATABASE_NAME = "EDMTDB.db"

        //Tables
        private const val TABLE_ROUTE_NAME = "Routes"
        private const val TABLE_POINT_NAME = "Points"
        private const val TABLE_SETT_NAME = "Settings"
        //Routes
        private const val COL_ROUTE_ID = "Id"
        private const val COL_ROUTE_NAME = "Name"
        private const val COL_ROUTE_SPEED = "Speed"
        //Points
        private const val COL_POINT_ROUTEID = "RouteId"
        private const val COL_POINT_ID = "Id"
        private const val COL_POINT_LATITUDE = "Latitude"
        private const val COL_POINT_LONGITUDE = "Longitude"
        private const val COL_POINT_LASTREFRESH = "LastRefresh"
        private const val COL_POINT_TIMEFROMPREV = "TimeFromPrevious"
        private const val COL_POINT_TIMESTAMP = "Timestamp"
        private const val COL_POINT_TEMP = "Temperature"
        private const val COL_POINT_POP = "ProbabilityOfPrecipitation"
        private const val COL_POINT_WINDSPD = "WindSpeed"
        private const val COL_POINT_WINDDEG = "WindDirection"
        private const val COL_POINT_WEATHER = "Weather"
        private const val COL_POINT_ICON = "Icon"
        //Settings
        private const val COL_SETT_SETTID = "SettingId"
        private const val COL_SETT_NAME = "Name"
        private const val COL_SETT_VALUE = "Value"

        //Settings records
        public const val SETT_TIMEDISTACC_NAME = "TimeDistAccuracy"
        public const val SETT_SPEED_NAME = "Speed"
        public const val SETT_APIKEY_NAME = "ApiKey"
        //Settings defaults
        public const val SETT_TIMEDISTACC_DEFAULT = "30"
        public const val SETT_SPEED_DEFAULT = "20"
        public const val SETT_APIKEY_DEFAULT = "Api Key"
    }

    override fun onCreate(db: SQLiteDatabase?) {
        val CREATE_ROUTE_TABLE_QUERY =
                ("CREATE TABLE $TABLE_ROUTE_NAME ($COL_ROUTE_ID INTEGER PRIMARY KEY, $COL_ROUTE_NAME TEXT, $COL_ROUTE_SPEED REAL)")
        val CREATE_POINT_TABLE_QUERY =
                ("CREATE TABLE $TABLE_POINT_NAME ($COL_POINT_ID INTEGER PRIMARY KEY, $COL_POINT_ROUTEID INTEGER, " +
                        "$COL_POINT_LATITUDE REAL, $COL_POINT_LONGITUDE REAL, " +
                        "$COL_POINT_LASTREFRESH INTEGER, $COL_POINT_TIMEFROMPREV INTEGER, $COL_POINT_TIMESTAMP INTEGER, " +
                        "$COL_POINT_TEMP REAL, $COL_POINT_POP REAL, $COL_POINT_WINDSPD REAL, " +
                        "$COL_POINT_WINDDEG INTEGER, $COL_POINT_WEATHER TEXT, $COL_POINT_ICON TEXT)")
        val CREATE_SETT_TABLE_QUERY =
                ("CREATE TABLE $TABLE_SETT_NAME ($COL_SETT_SETTID INTEGER PRIMARY KEY, $COL_SETT_NAME TEXT, $COL_SETT_VALUE TEXT)")
        db!!.execSQL(CREATE_ROUTE_TABLE_QUERY)
        db.execSQL(CREATE_POINT_TABLE_QUERY)
        db.execSQL(CREATE_SETT_TABLE_QUERY)
        setDefaultSettings(db)
    }

    override fun onUpgrade(db: SQLiteDatabase?, oldVersion: Int, newVersion: Int) {
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_ROUTE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_POINT_NAME")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SETT_NAME")
        onCreate(db)
    }

    fun clearDB() {
        val db = this.writableDatabase
        db!!.execSQL("DROP TABLE IF EXISTS $TABLE_ROUTE_NAME")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_POINT_NAME")
        db.execSQL("DROP TABLE IF EXISTS $TABLE_SETT_NAME")
        val CREATE_ROUTE_TABLE_QUERY =
                ("CREATE TABLE $TABLE_ROUTE_NAME ($COL_ROUTE_ID INTEGER PRIMARY KEY, $COL_ROUTE_NAME TEXT, $COL_ROUTE_SPEED REAL)")
        val CREATE_POINT_TABLE_QUERY =
                ("CREATE TABLE $TABLE_POINT_NAME ($COL_POINT_ID INTEGER PRIMARY KEY, $COL_POINT_ROUTEID INTEGER, " +
                        "$COL_POINT_LATITUDE REAL, $COL_POINT_LONGITUDE REAL, " +
                        "$COL_POINT_LASTREFRESH INTEGER, $COL_POINT_TIMEFROMPREV INTEGER, $COL_POINT_TIMESTAMP INTEGER, " +
                        "$COL_POINT_TEMP REAL, $COL_POINT_POP REAL, $COL_POINT_WINDSPD REAL, " +
                        "$COL_POINT_WINDDEG INTEGER, $COL_POINT_WEATHER TEXT, $COL_POINT_ICON TEXT)")
        val CREATE_SETT_TABLE_QUERY =
                ("CREATE TABLE $TABLE_SETT_NAME ($COL_SETT_SETTID INTEGER PRIMARY KEY, $COL_SETT_NAME TEXT, $COL_SETT_VALUE TEXT)")
        db.execSQL(CREATE_ROUTE_TABLE_QUERY)
        db.execSQL(CREATE_POINT_TABLE_QUERY)
        db.execSQL(CREATE_SETT_TABLE_QUERY)
        setDefaultSettings(db)
    }

    fun setDefaultSettings(db: SQLiteDatabase) {
        val insertTimeDistAccQuery = "INSERT INTO $TABLE_SETT_NAME ($COL_SETT_NAME, $COL_SETT_VALUE) VALUES ('$SETT_TIMEDISTACC_NAME', '$SETT_TIMEDISTACC_DEFAULT')"
        val insertSpeedQuery = "INSERT INTO $TABLE_SETT_NAME ($COL_SETT_NAME, $COL_SETT_VALUE) VALUES ('$SETT_SPEED_NAME', '$SETT_SPEED_DEFAULT')"
        val insertApiKeyQuery = "INSERT INTO $TABLE_SETT_NAME ($COL_SETT_NAME, $COL_SETT_VALUE) VALUES ('$SETT_APIKEY_NAME', '$SETT_APIKEY_DEFAULT')"
        db.execSQL(insertTimeDistAccQuery)
        db.execSQL(insertSpeedQuery)
        db.execSQL(insertApiKeyQuery)
    }

    fun getAllRoutes(): MutableList<RouteRecord> {
        val db = this.writableDatabase
        val selectQuery = "SELECT * FROM $TABLE_ROUTE_NAME"
        val cursor = db.rawQuery(selectQuery, null)
        val res = mutableListOf<RouteRecord>()
        while (cursor.moveToNext()) {
            val route = RouteRecord(
                cursor.getInt(cursor.getColumnIndex(COL_ROUTE_ID)),
                cursor.getString(cursor.getColumnIndex(COL_ROUTE_NAME)),
                cursor.getDouble(cursor.getColumnIndex(COL_ROUTE_SPEED))
            )
            res.add(route)
        }
        cursor.close()
        return res
    }

    fun addRoute(route: RouteRecord): Int {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COL_ROUTE_NAME, route.name)
        values.put(COL_ROUTE_SPEED, route.speed)
        val rowId = db.insert(TABLE_ROUTE_NAME, null, values)
        db.close()
        return rowId.toInt()
    }

    fun getRoute(routeId: Int): RouteRecord {
        val db = this.writableDatabase
        val selectQuery = "SELECT * FROM $TABLE_ROUTE_NAME WHERE $COL_ROUTE_ID=$routeId"
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToNext()) {
            return RouteRecord(
                cursor.getInt(cursor.getColumnIndex(COL_ROUTE_ID)),
                cursor.getString(cursor.getColumnIndex(COL_ROUTE_NAME)),
                cursor.getDouble(cursor.getColumnIndex(COL_ROUTE_SPEED))
            )
        } else {
            return RouteRecord(-1, "None", 0.0)
        }
    }

    fun rmRoute(rowId: Int) {
        val db = this.writableDatabase
        val deleteRouteQuery = "DELETE FROM $TABLE_ROUTE_NAME WHERE $COL_ROUTE_ID=$rowId"
        val deletePointsQuery = "DELETE FROM $TABLE_POINT_NAME WHERE $COL_POINT_ROUTEID=$rowId"
        db.execSQL(deleteRouteQuery)
        db.execSQL(deletePointsQuery)
        db.close()
    }

    fun getAllPoints(routeId: Int): MutableList<PointRecord> {
        val db = this.writableDatabase
        val selectQuery = "SELECT * FROM $TABLE_POINT_NAME WHERE $COL_POINT_ROUTEID=$routeId"
        val cursor = db.rawQuery(selectQuery, null)
        val res = mutableListOf<PointRecord>()
        while (cursor.moveToNext()) {
            val point = PointRecord(
                    cursor.getInt(cursor.getColumnIndex(COL_POINT_ROUTEID)),
                    cursor.getInt(cursor.getColumnIndex(COL_POINT_ID)),
                    cursor.getDouble(cursor.getColumnIndex(COL_POINT_LATITUDE)),
                    cursor.getDouble(cursor.getColumnIndex(COL_POINT_LONGITUDE)),
                    cursor.getInt(cursor.getColumnIndex(COL_POINT_LASTREFRESH)),
                    cursor.getInt(cursor.getColumnIndex(COL_POINT_TIMEFROMPREV)),
                    cursor.getInt(cursor.getColumnIndex(COL_POINT_TIMESTAMP)),
                    cursor.getDouble(cursor.getColumnIndex(COL_POINT_TEMP)),
                    cursor.getDouble(cursor.getColumnIndex(COL_POINT_POP)),
                    cursor.getDouble(cursor.getColumnIndex(COL_POINT_WINDSPD)),
                    cursor.getInt(cursor.getColumnIndex(COL_POINT_WINDDEG)),
                    cursor.getString(cursor.getColumnIndex(COL_POINT_WEATHER)),
                    cursor.getString(cursor.getColumnIndex(COL_POINT_ICON))
            )
            res.add(point)
        }
        cursor.close()
        return res
    }

    fun addPoint(point: PointRecord) {
        val db = this.writableDatabase
        val values = ContentValues()
        values.put(COL_POINT_ROUTEID, point.routeId)
        values.put(COL_POINT_LATITUDE, point.latitude)
        values.put(COL_POINT_LONGITUDE, point.longitude)
        values.put(COL_POINT_LASTREFRESH, point.lastRefresh)
        values.put(COL_POINT_TIMEFROMPREV, point.timeFromPrevious)
        values.put(COL_POINT_TIMESTAMP, point.timestamp)
        values.put(COL_POINT_TEMP, point.temp)
        values.put(COL_POINT_POP, point.pop)
        values.put(COL_POINT_WINDSPD, point.windSpd)
        values.put(COL_POINT_WINDDEG, point.windDeg)
        values.put(COL_POINT_WEATHER, point.weather)
        values.put(COL_POINT_ICON, point.icon)

        db.insert(TABLE_POINT_NAME, null, values)
        db.close()
    }

    fun popPoint(routeId: Int) {
        val db = this.writableDatabase
        val selectQuery = "SELECT MAX($COL_POINT_ID) FROM $TABLE_POINT_NAME WHERE $COL_POINT_ROUTEID=$routeId"
        val deleteQuery = "DELETE FROM $TABLE_POINT_NAME WHERE $COL_POINT_ID=($selectQuery)"
        db.execSQL(deleteQuery)
        db.close()
    }

    fun getLastPoint(routeId: Int): PointRecord? {
        val db = this.writableDatabase
        val whereQuery = "SELECT MAX($COL_POINT_ID) FROM $TABLE_POINT_NAME WHERE $COL_POINT_ROUTEID=$routeId"
        val selectQuery = "SELECT * FROM $TABLE_POINT_NAME WHERE $COL_POINT_ID=($whereQuery)"
        val cursor = db.rawQuery(selectQuery, null)
        if (cursor.moveToNext()) {
            return PointRecord(
                cursor.getInt(cursor.getColumnIndex(COL_POINT_ROUTEID)),
                cursor.getInt(cursor.getColumnIndex(COL_POINT_ID)),
                cursor.getDouble(cursor.getColumnIndex(COL_POINT_LATITUDE)),
                cursor.getDouble(cursor.getColumnIndex(COL_POINT_LONGITUDE)),
                cursor.getInt(cursor.getColumnIndex(COL_POINT_LASTREFRESH)),
                cursor.getInt(cursor.getColumnIndex(COL_POINT_TIMEFROMPREV)),
                cursor.getInt(cursor.getColumnIndex(COL_POINT_TIMESTAMP)),
                cursor.getDouble(cursor.getColumnIndex(COL_POINT_TEMP)),
                cursor.getDouble(cursor.getColumnIndex(COL_POINT_POP)),
                cursor.getDouble(cursor.getColumnIndex(COL_POINT_WINDSPD)),
                cursor.getInt(cursor.getColumnIndex(COL_POINT_WINDDEG)),
                cursor.getString(cursor.getColumnIndex(COL_POINT_WEATHER)),
                cursor.getString(cursor.getColumnIndex(COL_POINT_ICON))
            )
        } else {
            return null
        }
    }

    fun updatePoint(point: PointRecord) {
        val db = this.writableDatabase
        val updateQuery = "UPDATE $TABLE_POINT_NAME SET " +
                "$COL_POINT_LATITUDE=${point.latitude}, " +
                "$COL_POINT_LONGITUDE=${point.longitude}, " +
                "$COL_POINT_LASTREFRESH=${point.lastRefresh}, " +
                "$COL_POINT_TIMEFROMPREV=${point.timeFromPrevious}, " +
                "$COL_POINT_TIMESTAMP=${point.timestamp}, " +
                "$COL_POINT_TEMP=${point.temp}, " +
                "$COL_POINT_POP=${point.pop}, " +
                "$COL_POINT_WINDSPD=${point.windSpd}, " +
                "$COL_POINT_WINDDEG=${point.windDeg}, " +
                "$COL_POINT_WEATHER='${point.weather}', " +
                "$COL_POINT_ICON='${point.icon}' " +
                "WHERE $COL_POINT_ID=${point.id}"
        db.execSQL(updateQuery)
        db.close()
    }

    fun getSetting(settingName: String): String {
        val db = this.writableDatabase
        val selectQuery = "SELECT * FROM $TABLE_SETT_NAME WHERE $COL_SETT_NAME='$settingName'"
        val cursor = db.rawQuery(selectQuery, null)
        var res = ""
        if (cursor.moveToNext()) {
            res = cursor.getString(cursor.getColumnIndex(COL_SETT_VALUE))
        }
        cursor.close()
        return res
    }

    fun setSetting(settingName: String, settingValue: String) {
        val db = this.writableDatabase
        val updateQuery = "UPDATE $TABLE_SETT_NAME SET $COL_SETT_VALUE='$settingValue' WHERE $COL_SETT_NAME='$settingName'"
        db.execSQL(updateQuery)
        db.close()
    }
}