package com.example.routeplanner.database.records

data class PointRecord (
        var routeId: Int,
        var id: Int,
        var latitude: Double,
        var longitude: Double,
        var lastRefresh: Int,
        var timeFromPrevious: Int,
        var timestamp: Int,
        var temp: Double,
        var pop: Double,
        var windSpd: Double,
        var windDeg: Int,
        var weather: String,
        var icon: String
)