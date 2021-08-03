package com.example.routeplanner.ui.map

import org.osmdroid.util.GeoPoint

interface AnyMapFragment {
    fun setMapCenterToCurrentLocation()
    fun getMapCenter(): GeoPoint
    fun clearMapOverlays()
    fun addRouteToMapOverlays(routePoints: MutableList<GeoPoint>)
}