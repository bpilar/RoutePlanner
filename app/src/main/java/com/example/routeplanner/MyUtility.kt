package com.example.routeplanner

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.osmdroid.util.GeoPoint
import kotlin.math.*

class MyUtility {
    companion object {
        fun listToString(list: ArrayList<String>): String {
            val gson = Gson()
            val json = gson.toJson(list)
            return json
        }

        fun stringToList(json: String): ArrayList<String> {
            val gson = Gson()
            val type = object : TypeToken<ArrayList<String>>(){}.type
            val list: ArrayList<String> = gson.fromJson(json, type)
            return list
        }

        fun geopointToString(geopoint: GeoPoint): String {
            val gson = Gson()
            val json = gson.toJson(geopoint)
            return json
        }

        fun stringToGeopoint(json: String): GeoPoint {
            val gson = Gson()
            val type = object : TypeToken<GeoPoint>(){}.type
            val geopoint: GeoPoint = gson.fromJson(json, type)
            return geopoint
        }

        fun routeToString(route: MutableList<GeoPoint>): String {
            val jsonRoute = arrayListOf<String>()
            for (point in route)
                jsonRoute.add(geopointToString(point))
            return listToString(jsonRoute)
        }

        fun routesToString(routes: MutableList<MutableList<GeoPoint>>): String {
            val jsonRoutes = arrayListOf<String>()
            for (route in routes)
                jsonRoutes.add(routeToString(route))
            return listToString(jsonRoutes)
        }

        fun stringToRoute(jsonRoute: String): MutableList<GeoPoint> {
            val route = stringToList(jsonRoute)
            val restoredRoute = mutableListOf<GeoPoint>()
            for (jsonPoint in route) {
                val point = stringToGeopoint(jsonPoint)
                restoredRoute.add(point)
            }
            return restoredRoute
        }

        fun stringToRoutes(jsonRoutes: String): MutableList<MutableList<GeoPoint>> {
            val routes = stringToList(jsonRoutes)
            val restoredRoutes = mutableListOf<MutableList<GeoPoint>>()
            for (jsonRoute in routes)
                restoredRoutes.add(stringToRoute(jsonRoute))
            return restoredRoutes
        }

        fun distanceBetweenPoints(first: GeoPoint, second: GeoPoint): Double {
            val R = 6371000 //earth radius
            val lat1 = first.latitude * PI / 180
            val lat2 = second.latitude * PI / 180
            val diffLat = (second.latitude - first.latitude) * PI / 180
            val diffLon = (second.longitude - first.longitude) * PI / 180

            val a = sin(diffLat / 2) * sin(diffLat / 2) +
                    cos(lat1) * cos(lat2) *
                    sin(diffLon / 2) * sin(diffLon / 2)
            val c = 2 * atan2(sqrt(a), sqrt(1 - a))
            return R * c
        }

        fun getNPointsBetween(first: GeoPoint, second: GeoPoint, nPoints: Int): MutableList<GeoPoint> {
            val diffLat = second.latitude - first.latitude
            val diffLon = second.longitude - first.longitude
            val stepLat = diffLat / (nPoints + 1)
            val stepLon = diffLon / (nPoints + 1)
            val res = mutableListOf<GeoPoint>()
            for (index in 1..nPoints) {
                res.add(GeoPoint(first.latitude + (stepLat * index), first.longitude + (stepLon * index)))
            }
            return res
        }

        fun kmphTOmps(kmph: Double): Double {
            return kmph * 5 / 18
        }

        fun mpsTOkmph(mps: Double): Double {
            return mps * 18 / 5
        }

        fun minTOsek(min: Int): Int {
            return min * 60
        }
    }
}