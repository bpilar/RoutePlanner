package com.example.routeplanner.ui.routes

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.routeplanner.R
import com.example.routeplanner.database.DBHelper


class RouteFragment : Fragment() {
    private lateinit var db: DBHelper
    private lateinit var root: View
    private lateinit var pointAdapter: PointAdapter
    private lateinit var deleteButton: Button
    private lateinit var pointsRecyclerView: RecyclerView
    private var routeId: Int = -1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            routeId = it.getInt(ARG_ROUTEID)
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        root = inflater.inflate(R.layout.fragment_route, container, false)
        db = DBHelper(requireContext())
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        deleteButton = root.findViewById<Button>(R.id.delete_button)
        pointsRecyclerView = root.findViewById<RecyclerView>(R.id.points_recycler_view)

        val points = db.getAllPoints(routeId)
        pointAdapter = PointAdapter(points, requireContext())
        pointsRecyclerView.adapter = pointAdapter
        pointsRecyclerView.layoutManager = LinearLayoutManager(requireContext())

        deleteButton.setOnClickListener {
            db.rmRoute(routeId)

            requireActivity().onBackPressed()
        }
    }

    companion object {
        fun newInstance(param1: String) =
                RouteFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_ROUTEID, param1)
                    }
                }
        const val ARG_ROUTEID = "route_id"
    }
}