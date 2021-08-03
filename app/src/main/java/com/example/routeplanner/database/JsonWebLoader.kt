package com.example.routeplanner.database

import com.example.routeplanner.database.records.PointRecord
import org.json.JSONException
import org.json.JSONObject
import java.lang.Exception
import java.net.URL
import kotlin.math.roundToInt

class JsonWebLoader {
    companion object {

        fun reloadPoints(routeId: Int, db: DBHelper, apiKey: String) {
            val points = db.getAllPoints(routeId)
            points.sortBy { it.lastRefresh }
            for (point in points) {
                reloadPoint(point, db, apiKey)
            }
        }

        private fun reloadPoint(point: PointRecord, db: DBHelper, apiKey: String) {
            val url = "https://api.openweathermap.org/data/2.5/onecall?lat=" +
                    point.latitude.toString() + "&lon=" + point.latitude.toString() +
                    "&exclude=minutely,daily,alerts&units=metric&lang=en&appid=" + apiKey
            var body = ""
            try {
                body = URL(url).readText()
            } catch (exception: Exception) {
                return
            }
            if (body != "") {
                val jsonAll = JSONObject(body)
                try {
                    jsonAll.getString("timezone")
                } catch (exception: JSONException) {
                    return
                }
                val current = jsonAll.getJSONObject("current")
                val dt = current.getInt("dt")
                val hourly = jsonAll.getJSONArray("hourly")
                val hoursDts = mutableListOf<Int>()
                for (index in 0 until hourly.length()) {
                    hoursDts.add(hourly.getJSONObject(index).getInt("dt"))
                }
                var prevIdx = 0
                var nextIdx = hoursDts.lastIndex
                for (index in 0 until hoursDts.size) {
                    if (hoursDts[index] <= dt)
                        prevIdx = index
                }
                for (index in hoursDts.size - 1 downTo 0) {
                    if (dt <= hoursDts[index])
                        nextIdx = index
                }
                val prevHour = hourly.getJSONObject(prevIdx)
                val nextHour = hourly.getJSONObject(nextIdx)
                val prevDt = prevHour.getInt("dt")
                val nextDt = nextHour.getInt("dt")
                val diffDt = nextDt - prevDt
                var prevWeight = 1.0
                var nextWeight = 0.0
                if (diffDt != 0) {
                    prevWeight = (dt - prevDt) / diffDt.toDouble()
                    nextWeight = 1 - prevWeight
                }
                val closestHour = if (prevWeight < 0.5)  hourly.getJSONObject(prevIdx) else hourly.getJSONObject(nextIdx)
                val temp = prevHour.getDouble("temp") * prevWeight + nextHour.getDouble("temp") * nextWeight
                val pop = prevHour.getDouble("pop") * prevWeight + nextHour.getDouble("pop") * nextWeight
                val windSpd = prevHour.getDouble("wind_speed") * prevWeight + nextHour.getDouble("wind_speed") * nextWeight
                val windDeg = (prevHour.getInt("wind_deg") * prevWeight + nextHour.getInt("wind_deg") * nextWeight).roundToInt()
                val weather = closestHour.getJSONArray("weather").getJSONObject(0).getString("description")
                val icon = closestHour.getJSONArray("weather").getJSONObject(0).getString("icon")

                point.lastRefresh = dt
                point.timestamp = dt + point.timeFromPrevious
                point.temp = temp
                point.pop = pop
                point.windSpd = windSpd
                point.windDeg = windDeg
                point.weather = weather
                point.icon = icon

                db.updatePoint(point)
            }
        }

        fun validateKey(apiKey: String): Boolean {
//            val url = "https://api.openweathermap.org/data/2.5/onecall?lat=0.0&lon=0.0&exclude=minutely,daily,alerts&units=metric&lang=en&appid=$apiKey"
            val url = "https://api.openweathermap.org/data/2.5/onecall?lat=" +
                    0.0.toString() + "&lon=" + 0.0.toString() +
                    "&exclude=minutely,daily,alerts&units=metric&lang=en&appid=" + apiKey
            var body = ""
            try {
                body = URL(url).readText()
                val jsonAll = JSONObject(body)
                jsonAll.getString("timezone")
                return true
            } catch (exception: Exception) {
                return false
            }
        }

    }
}