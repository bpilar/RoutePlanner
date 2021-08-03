package com.example.routeplanner.ui.routes

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.routeplanner.R
import com.example.routeplanner.database.records.RouteRecord

class RouteAdapter(private val routes: MutableList<RouteRecord>, private val routeClickListener: RouteClickListener): RecyclerView.Adapter<RouteAdapter.RouteViewHolder>() {
    class RouteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val name: TextView = itemView.findViewById(R.id.route_name)
        private val speed: TextView = itemView.findViewById(R.id.route_speed)

        fun bind(curUser: RouteRecord) {
            name.text = curUser.name
            speed.text = curUser.speed.toString()
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RouteViewHolder {
        return RouteViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.item_route,
                parent,
                false
            )
        )
    }

    fun addRoute(route: RouteRecord) {
        routes.add(route)
        notifyItemInserted(routes.size - 1)
    }

    fun repopulate(newRoutes: MutableList<RouteRecord>) {
        routes.clear()
        routes.addAll(newRoutes)
        notifyDataSetChanged()
    }

    override fun onBindViewHolder(holder: RouteViewHolder, position: Int) {
        val curRoute = routes[position]
        holder.bind(curRoute)
        holder.itemView.setOnClickListener {
            routeClickListener.onRouteClickListener(curRoute)
        }
    }

    override fun getItemCount(): Int {
        return routes.size
    }
}