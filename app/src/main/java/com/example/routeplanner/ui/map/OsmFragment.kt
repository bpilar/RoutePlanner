package com.example.routeplanner.ui.map

import android.annotation.SuppressLint
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.os.Looper
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.routeplanner.MyUtility
import com.example.routeplanner.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker
import org.osmdroid.views.overlay.Overlay
import org.osmdroid.views.overlay.OverlayManager
import org.osmdroid.views.overlay.Polyline


class OsmFragment : Fragment(), AnyMapFragment  {
    private lateinit var root: View
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var osmMapView: MapView
    private lateinit var osmController: MapController
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var locationCallback: LocationCallback
    private lateinit var routesList: MutableList<MutableList<GeoPoint>>

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.fragment_osm, container, false)

        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        osmMapView = root.findViewById(R.id.osm_map)
        osmMapView.setTileSource(TileSourceFactory.MAPNIK)
        osmMapView.isTilesScaledToDpi = true
        osmMapView.setMultiTouchControls(true)
        osmMapView.zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)

        val osmContributors = root.findViewById<TextView>(R.id.osm_contributors)
        osmContributors.movementMethod = LinkMovementMethod.getInstance()

        osmController = osmMapView.controller as MapController

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())
        locationRequest = LocationRequest.create()
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            .setInterval(20 * 1000)
            .setFastestInterval(2 * 1000)
        locationCallback = object : LocationCallback() { }

        routesList = mutableListOf<MutableList<GeoPoint>>()

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val lastLatitude = sharedPreferences.getString(PREFS_LATITUDE, defaultLatitude)!!.toDouble()
        val lastLongitude = sharedPreferences.getString(PREFS_LONGITUDE, defaultLongitude)!!.toDouble()
        val lastOrientation = sharedPreferences.getString(PREFS_ORIENTATION, defaultOrientation)!!.toFloat()
        val lastZoom = sharedPreferences.getString(PREFS_ZOOM, defaultZoom)!!.toDouble()
        osmController.setCenter(GeoPoint(lastLatitude, lastLongitude))
        osmMapView.mapOrientation = lastOrientation
        osmController.setZoom(lastZoom)
        osmMapView.invalidate()
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
//        Toast.makeText(context, "Location updates revoked", Toast.LENGTH_SHORT).show()
    }

    override fun onPause() {
        super.onPause()
        val edit = sharedPreferences.edit()
        edit.putString(PREFS_TILE_SOURCE_NAME, osmMapView.tileProvider.tileSource.name())
        edit.putString(PREFS_LATITUDE, osmMapView.boundingBox.centerLatitude.toString())
        edit.putString(PREFS_LONGITUDE, osmMapView.boundingBox.centerLongitude.toString())
        edit.putString(PREFS_ORIENTATION, osmMapView.mapOrientation.toString())
        edit.putString(PREFS_ZOOM, osmMapView.zoomLevelDouble.toString())
        edit.putString(PREFS_ROUTES_SERIALIZED, MyUtility.routesToString(routesList))
        clearMapOverlays()
        edit.apply()
        stopLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    private fun startLocationUpdates() {
        fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
//        Toast.makeText(context, "Location updates requested", Toast.LENGTH_SHORT).show()
    }

    override fun onResume() {
        super.onResume()
        val tileSourceName = sharedPreferences.getString(PREFS_TILE_SOURCE_NAME, TileSourceFactory.DEFAULT_TILE_SOURCE.name())
        val lastLatitude = sharedPreferences.getString(PREFS_LATITUDE, defaultLatitude)!!.toDouble()
        val lastLongitude = sharedPreferences.getString(PREFS_LONGITUDE, defaultLongitude)!!.toDouble()
        val lastOrientation = sharedPreferences.getString(PREFS_ORIENTATION, defaultOrientation)!!.toFloat()
        val lastZoom = sharedPreferences.getString(PREFS_ZOOM, defaultZoom)!!.toDouble()
        val tileSource = TileSourceFactory.getTileSource(tileSourceName)
        val jsonRoutesList = sharedPreferences.getString(PREFS_ROUTES_SERIALIZED, null)
        osmMapView.setTileSource(tileSource)
        osmController.setCenter(GeoPoint(lastLatitude, lastLongitude))
        osmMapView.mapOrientation = lastOrientation
        osmController.setZoom(lastZoom)
        if (jsonRoutesList != null) {
            val restoredRoutesList = MyUtility.stringToRoutes(jsonRoutesList)
            routesList.addAll(restoredRoutesList)
            for (route in restoredRoutesList)
                addRouteToMapOverlays(route)
        }
        osmMapView.invalidate()
        startLocationUpdates()
    }

    @SuppressLint("MissingPermission")
    override fun setMapCenterToCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener {
            if (it != null) {
                osmController.animateTo(GeoPoint(it.latitude, it.longitude))
            } else {
                Toast.makeText(context, "Location unavailable", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getMapCenter(): GeoPoint {
        return GeoPoint(osmMapView.boundingBox.centerLatitude, osmMapView.boundingBox.centerLongitude)
    }

    override fun clearMapOverlays() {
        routesList.clear()
        osmMapView.overlays.clear()
        osmMapView.invalidate()
    }

    override fun addRouteToMapOverlays(routePoints: MutableList<GeoPoint>) {
        routesList.add(routePoints)
        for (point in routePoints)
            addPoint(point)
        addPolyline(routePoints)
        osmMapView.invalidate()
    }

    private fun addPoint(position: GeoPoint) {
        val point = Marker(osmMapView)
        point.position = position
        point.setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
        osmMapView.overlays.add(point)
    }

    private fun addPolyline(positions: List<GeoPoint>) {
        val polyline = Polyline(osmMapView)
        polyline.setPoints(positions)
        osmMapView.overlays.add(polyline)
    }

    companion object {
        fun newInstance() = OsmFragment()

        private const val PREFS_NAME = "org.osm.oms_preferences"
        private const val PREFS_TILE_SOURCE_NAME = "osm_tilesource"
        private const val PREFS_LATITUDE = "osm_latitude"
        private const val PREFS_LONGITUDE = "osm_longitude"
        private const val PREFS_ORIENTATION = "osm_orientation"
        private const val PREFS_ZOOM = "osm_zoom"
        private const val PREFS_ROUTES_SERIALIZED = "osm_routes_serialized"

        private const val defaultLatitude = "51.9176785"
        private const val defaultLongitude = "19.134415"
        private const val defaultOrientation = "0.0F"
        private const val defaultZoom = "6.0"
    }
}