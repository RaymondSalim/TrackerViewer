package com.reas.trackerviewer.locationHistory

import android.content.Context
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.reas.trackerviewer.R
import java.util.*

class LocationHistoryRecyclerView (
    private val context: Context,
    private val data: ArrayList<LocationBaseObject>): RecyclerView.Adapter<LocationHistoryRecyclerView.Holder>() {
    private val layoutInflater = LayoutInflater.from(context)
    private val geoCoder = Geocoder(context, Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(layoutInflater.inflate(R.layout.recyclerview_location, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(data[position])
    }

    override fun getItemCount(): Int = data.size

    inner class Holder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.name)
        val address = itemView.findViewById<TextView>(R.id.address)
        val mapsIcon = itemView.findViewById<ImageView>(R.id.imageIcon)

        fun bind(location: LocationBaseObject) {
            val currentAddress = geoCoder.getFromLocation(location.mLatitude, location.mLongitude, 1)
            name.text = currentAddress[0].featureName ?: ""
            address.text = currentAddress[0].getAddressLine(0)
        }
    }
}