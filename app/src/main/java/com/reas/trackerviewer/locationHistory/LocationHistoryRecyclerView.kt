package com.reas.trackerviewer.locationHistory

import android.content.Context
import android.location.Geocoder
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.model.LatLng
import com.reas.trackerviewer.MainActivity
import com.reas.trackerviewer.R
import java.text.SimpleDateFormat
import java.util.*

class LocationHistoryRecyclerView (
    private val context: Context,
    private val data: ArrayList<LocationBaseObject>): RecyclerView.Adapter<LocationHistoryRecyclerView.Holder>() {
    private val layoutInflater = LayoutInflater.from(context)
    private val geoCoder = Geocoder(context, Locale.getDefault())

    private val fm = (context as MainActivity).supportFragmentManager
    private val locationHistoryFragment = fm.findFragmentByTag("locationHistoryFragment") as LocationHistoryFragment
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(layoutInflater.inflate(R.layout.recyclerview_location, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val currentData = data[position]
//        holder.bind(currentData)
        val format = SimpleDateFormat("HH:mm", Locale.getDefault())

        holder.latitude = currentData.mLatitude
        holder.longitude = currentData.mLongitude

        val currentAddress = geoCoder.getFromLocation(currentData.mLatitude, currentData.mLongitude, 1)
        holder.name.text = currentAddress[0].featureName ?: ""
        holder.address.text = currentAddress[0].getAddressLine(0)
        holder.time.text = format.format(currentData.mTime)

        holder.itemView.setOnClickListener {
            Toast.makeText(context, "${holder.name.text}", Toast.LENGTH_SHORT).show()
            val latLng = LatLng(currentData.mLatitude, currentData.mLongitude)
            locationHistoryFragment.setMap(LocationGeocoderObject(latLng, holder.name.text as String))
            locationHistoryFragment.setLastLocation(latLng)
        }
    }

    override fun getItemCount(): Int = data.size

    inner class Holder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val name = itemView.findViewById<TextView>(R.id.name)
        val address = itemView.findViewById<TextView>(R.id.address)
        val time = itemView.findViewById<TextView>(R.id.time)
        val mapsIcon = itemView.findViewById<ImageView>(R.id.imageIcon)
        var latitude = 0.0
        var longitude = 0.0



//        fun bind(location: LocationBaseObject) {
//            latitude = location.mLatitude
//            longitude = location.mLongitude
//
//            val currentAddress = geoCoder.getFromLocation(latitude, longitude, 1)
//            name.text = currentAddress[0].featureName ?: ""
//            address.text = currentAddress[0].getAddressLine(0)
//
//
//        }
    }
}