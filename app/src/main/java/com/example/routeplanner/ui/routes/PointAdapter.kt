package com.example.routeplanner.ui.routes

import android.content.Context
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.routeplanner.R
import com.example.routeplanner.database.records.PointRecord
//import com.koushikdutta.urlimageviewhelper.UrlImageViewHelper
import com.squareup.picasso.Picasso


class PointAdapter(private val points: MutableList<PointRecord>, private val context: Context): RecyclerView.Adapter<PointAdapter.PointViewHolder>() {
    class PointViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.point_name)
        private val lastRefreshed: TextView = itemView.findViewById(R.id.point_last_refreshed)
        private val timestamp: TextView = itemView.findViewById(R.id.point_timestamp)
        private val temp: TextView = itemView.findViewById(R.id.point_temp)
        private val pop: TextView = itemView.findViewById(R.id.point_pop)
        private val windSpd: TextView = itemView.findViewById(R.id.point_wind_spd)
        private val windDeg: TextView = itemView.findViewById(R.id.point_wind_deg)
        private val weatherName: TextView = itemView.findViewById(R.id.point_weather_name)
        private val weatherIcon: ImageView = itemView.findViewById(R.id.point_weather_icon)

        fun bind(currPoint: PointRecord, context: Context) {
            val windDirection = when {
                currPoint.windDeg < 23 -> "N"
                currPoint.windDeg < 68 -> "NE"
                currPoint.windDeg < 113 -> "E"
                currPoint.windDeg < 158 -> "SE"
                currPoint.windDeg < 203 -> "S"
                currPoint.windDeg < 248 -> "SW"
                currPoint.windDeg < 293 -> "W"
                currPoint.windDeg < 338 -> "NW"
                else -> "N"
            }

            name.text = String.format("%.3f", currPoint.latitude) + " : " + String.format("%.3f", currPoint.longitude)
            lastRefreshed.text = currPoint.lastRefresh.toString()
            timestamp.text = currPoint.timestamp.toString()
            temp.text = String.format("%.1f", currPoint.temp)
            pop.text = (currPoint.pop * 100).toInt().toString()
            windSpd.text = String.format("%.1f", currPoint.windSpd)
            windDeg.text = windDirection
            weatherName.text = currPoint.weather

            Picasso.with(context)
                    .load("https://openweathermap.org/img/wn/${currPoint.icon}@2x.png")
                    .placeholder(R.drawable.ic_loading)
                    .into(weatherIcon)
//            UrlImageViewHelper.setUrlDrawable(weatherIcon, "https://openweathermap.org/img/wn/${currPoint.icon}@2x.png", R.drawable.ic_loading)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PointViewHolder {
        return PointViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.item_point, parent, false))
    }

    fun addPoint(point: PointRecord) {
        points.add(point)
        notifyItemInserted(points.size - 1)
    }

    override fun onBindViewHolder(holder: PointViewHolder, position: Int) {
        val curPoint = points[position]
        holder.bind(curPoint, context)
    }

    override fun getItemCount(): Int {
        return points.size
    }
}