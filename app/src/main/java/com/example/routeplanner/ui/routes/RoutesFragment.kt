package com.example.routeplanner.ui.routes

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.routeplanner.R
import com.example.routeplanner.database.DBHelper
import com.example.routeplanner.database.JsonWebLoader
import com.example.routeplanner.database.records.RouteRecord

class RoutesFragment : Fragment(), RouteClickListener {
    private lateinit var db: DBHelper
    private lateinit var root: View
    private lateinit var routeAdapter: RouteAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.fragment_routes, container, false)
        db = DBHelper(requireContext())
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        routeAdapter = RouteAdapter(db.getAllRoutes(), this)
        val routesRecyclerView = root.findViewById<RecyclerView>(R.id.routes_recycler_view)
        routesRecyclerView.adapter = routeAdapter
        routesRecyclerView.layoutManager = LinearLayoutManager(requireContext())
    }

    override fun onStart() {
        super.onStart()
        routeAdapter.repopulate(db.getAllRoutes())
    }

    override fun onRouteClickListener(route: RouteRecord) {
        Thread {
            kotlin.run {
                val apiKey = db.getSetting(DBHelper.SETT_APIKEY_NAME)
                JsonWebLoader.reloadPoints(route.id, db, apiKey)
                requireActivity().runOnUiThread() {
                    val bundle = bundleOf(RouteFragment.ARG_ROUTEID to route.id)
                    findNavController().navigate(R.id.action_navigation_routes_to_routeFragment, bundle)
                }
            }
        }.start()
    }
}