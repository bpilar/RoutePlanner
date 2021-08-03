package com.example.routeplanner.ui.map

import android.app.Dialog
import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.routeplanner.MyUtility
import com.example.routeplanner.R
import com.example.routeplanner.database.DBHelper
import com.example.routeplanner.database.records.PointRecord
import com.example.routeplanner.database.records.RouteRecord
import org.osmdroid.util.GeoPoint

class MapFragment : Fragment() {
    private lateinit var db: DBHelper
    private lateinit var root: View
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var anyMapFragment: AnyMapFragment
    private lateinit var myLocationButton: ImageButton
    private lateinit var addLocationButton: ImageButton
    private lateinit var removeLocationButton: ImageButton
    private lateinit var saveLocationButton: ImageButton
    private var currentRouteId = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val cfm = childFragmentManager
        val mf = cfm.findFragmentById(R.id.map_container)
        if (mf == null) {
            val nmf = OsmFragment.newInstance()
            cfm.beginTransaction()
                    .replace(R.id.map_container, nmf)
                    .commit()
            anyMapFragment = nmf
        } else {
            anyMapFragment = mf as AnyMapFragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.fragment_map, container, false)
        db = DBHelper(requireContext())
        sharedPreferences = requireContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initGUI()
    }

    private fun initGUI() {
        myLocationButton = root.findViewById<ImageButton>(R.id.my_location_button)
        addLocationButton = root.findViewById<ImageButton>(R.id.add_location_button)
        removeLocationButton = root.findViewById<ImageButton>(R.id.remove_location_button)
        saveLocationButton = root.findViewById<ImageButton>(R.id.save_location_button)

        myLocationButton.setOnClickListener { onMyLocationButtonClicked() }
        addLocationButton.setOnClickListener { onAddLocationButtonClicked() }
        removeLocationButton.setOnClickListener { onRemoveLocationButtonClicked() }
        saveLocationButton.setOnClickListener { onSaveLocationButtonClicked() }
    }

    override fun onPause() {
        super.onPause()
        val edit = sharedPreferences.edit()
        edit.putInt(PREFS_CURRENT_ROUTE_ID, currentRouteId)
        edit.apply()
        redrawRoads()
    }

    override fun onResume() {
        super.onResume()
        val readRouteId = sharedPreferences.getInt(PREFS_CURRENT_ROUTE_ID, -1)
        currentRouteId = readRouteId
        redrawRoads()
    }

    private fun onMyLocationButtonClicked() {
        anyMapFragment.setMapCenterToCurrentLocation()
        redrawRoads()
    }

    private fun onAddLocationButtonClicked() {
        val currentPoint = anyMapFragment.getMapCenter()
        if (currentRouteId < 0) {
            val dialog = createNewRouteDialog(currentPoint)
            dialog.show()
        } else {
            val previousPoint = db.getLastPoint(currentRouteId)
            if (previousPoint != null) {
                val previousGeoPoint = GeoPoint(previousPoint.latitude, previousPoint.longitude)
                val distanceToCurrent = MyUtility.distanceBetweenPoints(previousGeoPoint, currentPoint)
                val speedMps = MyUtility.kmphTOmps(db.getRoute(currentRouteId).speed)
                val timeToCurrent = (distanceToCurrent / speedMps).toInt()
                val timeAccuracy = MyUtility.minTOsek(db.getSetting(DBHelper.SETT_TIMEDISTACC_NAME).toInt())
                val midpointsCount = (timeToCurrent / timeAccuracy).toInt()
                if (midpointsCount > 0) {
                    val timeStep = (timeToCurrent / (midpointsCount + 1)).toInt()
                    val midPoints = MyUtility.getNPointsBetween(previousGeoPoint, currentPoint, midpointsCount)
                    for ((index, midPoint) in midPoints.withIndex()) {
                        val prevTime = previousPoint.timeFromPrevious
                        db.addPoint(PointRecord(currentRouteId, -1, midPoint.latitude, midPoint.longitude, 0, prevTime + ((index + 1) * timeStep), 0, 0.0, 0.0, 0.0, 0, "None", "none"))
                    }
                }
                val prevTime = previousPoint.timeFromPrevious
                db.addPoint(PointRecord(currentRouteId, -1, currentPoint.latitude, currentPoint.longitude, 0, prevTime + timeToCurrent, 0, 0.0, 0.0, 0.0, 0, "None", "none"))
            } else {
                db.addPoint(PointRecord(currentRouteId, -1, currentPoint.latitude, currentPoint.longitude, 0, 0, 0, 0.0, 0.0, 0.0, 0, "None", "none"))
            }
        }


        redrawRoads()
    }

    private fun onRemoveLocationButtonClicked() {
        db.popPoint(currentRouteId)

        redrawRoads()
    }

    private fun onSaveLocationButtonClicked() {
        currentRouteId = -1
        redrawRoads()
    }

    private fun redrawRoads() {
        val remainingPoints = db.getAllPoints(currentRouteId)
        if (remainingPoints.isEmpty()) {
            db.rmRoute(currentRouteId)
            currentRouteId = -1
        }

        val currentRoute = db.getAllPoints(currentRouteId).map{ GeoPoint(it.latitude, it.longitude) }.toMutableList()
        anyMapFragment.clearMapOverlays()
        anyMapFragment.addRouteToMapOverlays(currentRoute)
    }

    private fun createNewRouteDialog(currentPoint: GeoPoint): Dialog {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.dialog_new_route)
        val addButton = dialog.findViewById<Button>(R.id.add_button)
        val newNameEdit = dialog.findViewById<EditText>(R.id.new_name_edit)
        addButton.setOnClickListener {
            val speed = db.getSetting(DBHelper.SETT_SPEED_NAME).toDouble()
            currentRouteId = addNewRoute(newNameEdit.text.toString(), speed)
            db.addPoint(PointRecord(currentRouteId, -1, currentPoint.latitude, currentPoint.longitude, 0, 0, 0, 0.0, 0.0, 0.0, 0, "None", "none"))
            redrawRoads()
            dialog.dismiss()
        }
        return dialog
    }

    private fun addNewRoute(name: String, speed: Double): Int {
        return db.addRoute(RouteRecord(-1, name, speed))
    }

    companion object {
        private const val PREFS_NAME = "org.routeplanner.routeplanner_preferences"
        private const val PREFS_CURRENT_ROUTE_ID = "current_route_id"
    }

}