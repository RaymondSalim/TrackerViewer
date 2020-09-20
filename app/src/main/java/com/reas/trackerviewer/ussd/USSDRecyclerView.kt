package com.reas.trackerviewer.ussd

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.reas.trackerviewer.R
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class USSDRecyclerView(
    private val context: Context,
    private val data: HashMap<String, String>
): RecyclerView.Adapter<USSDRecyclerView.Holder>() {
    private val layoutInflater = LayoutInflater.from(context)

    private var keyList: ArrayList<String>? = null

    init {
        keyList = ArrayList(data.keys)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): USSDRecyclerView.Holder {
        return Holder(layoutInflater.inflate(R.layout.recyclerview_ussd, parent, false))
    }

    override fun onBindViewHolder(holder: USSDRecyclerView.Holder, position: Int) {
        val time = keyList?.get(position)

        holder.summary.text = data[time]

        var ldt: LocalDateTime = LocalDateTime.parse(time)
        var dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy")
        holder.date.text = dateFormatter.format(ldt)

        var timeFormatter = DateTimeFormatter.ofPattern("HH:m:s")
        holder.time.text = timeFormatter.format(ldt)
    }

    override fun getItemCount(): Int = keyList!!.size

    class Holder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var summary: TextView
        var date: TextView
        var time: TextView

        init {
            with (itemView) {
                summary = findViewById(R.id.ussdSummary)
                date = findViewById(R.id.date)
                time = findViewById(R.id.time)
            }
        }
    }
}