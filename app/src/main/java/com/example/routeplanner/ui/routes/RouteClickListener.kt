package com.example.routeplanner.ui.routes

import com.example.routeplanner.database.records.RouteRecord

interface RouteClickListener {
    fun onRouteClickListener(user: RouteRecord)
}