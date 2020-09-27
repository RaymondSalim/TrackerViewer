package com.reas.trackerviewer.locationHistory

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.reas.trackerviewer.R

class LocationHistoryRecyclerView (
    private val context: Context,
    private val data: ArrayList<LocationBaseObject>): RecyclerView.Adapter<LocationHistoryRecyclerView.Holder>() {
    private val layoutInflater = LayoutInflater.from(context)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(layoutInflater.inflate(R.layout.recyclerview_location, parent, false))
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        TODO("Not yet implemented")
    }

    override fun getItemCount(): Int = data.size

    class Holder(itemView: View): RecyclerView.ViewHolder(itemView) {

    }
}