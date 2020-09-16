package com.reas.trackerviewer.calls

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.view.marginLeft
import androidx.recyclerview.widget.RecyclerView
import com.reas.trackerviewer.R
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class CallRecyclerView(
    private val context: Context,
    private val list: HashMap<String, ArrayList<CallBaseObject>>
): RecyclerView.Adapter<CallRecyclerView.Holder>() {
    private var listOfKeys: ArrayList<String>
    private val layoutInflater = LayoutInflater.from(context)

    init {
        val keySet:Set<String> = list.keys
        listOfKeys = ArrayList(keySet)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        return Holder(
            layoutInflater.inflate(
                R.layout.recyclerview_calls,
                null,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val sorted: SortedMap<String, CallBaseObject> = sortHashMap(list)
        listOfKeys = ArrayList<String>(sorted.keys)
        var callBaseObject: CallBaseObject = sorted[listOfKeys[position]] as CallBaseObject

        // Set the call number
        holder.callerNumber.text = listOfKeys[position]

        // Set call duration
        var duration = callBaseObject.mDuration

        when {
            duration == 0L -> {
//                holder.callDuration.text = ""
                holder.callDuration.visibility = View.GONE
            }
            duration < 60 -> {
                holder.callDuration.text = "$duration sec"
            }
            else -> {
                val minutes = (duration / 60).toInt()
                val seconds = duration % 60
                holder.callDuration.text = "${minutes}m ${seconds}s"
            }
        }

        // Set call direction logo
        when (callBaseObject.mDirection) {
            "Incoming" -> {
                holder.callDirection.setImageResource(R.drawable.ic_baseline_call_received_24) }

            "Outgoing" -> {
                holder.callDirection.setImageResource(R.drawable.ic_baseline_call_made_24) }

            "Missed" -> {
                holder.callDirection.setImageResource(R.drawable.ic_baseline_call_missed_24) }

            else -> {
                holder.callDirection.setImageResource(R.drawable.ic_baseline_call_24) }
        }

        // Set call time
        val calendar = Calendar.getInstance()
        calendar.timeZone = TimeZone.getDefault()
        calendar.timeInMillis = callBaseObject.mTime

        val dateFormat = SimpleDateFormat("HH:mm dd/MM/yyyy")
        dateFormat.timeZone = TimeZone.getDefault()

        holder.dateTime.text = dateFormat.format(calendar.time)

    }

    private fun sortHashMap(hashMap: HashMap<String, ArrayList<CallBaseObject>>): SortedMap<String, CallBaseObject> {
        val output: HashMap<String, CallBaseObject> = HashMap<String, CallBaseObject>()
        hashMap.forEach {
            val key = it.key
            val array = it.value
            val callBaseObject = array[array.size - 1]
            output[key] = callBaseObject
        }

        return output.toSortedMap(compareByDescending { output[it]?.mTime })
    }


    override fun getItemCount(): Int = listOfKeys.size


    class Holder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var callerNumber: TextView
        var dateTime: TextView
        var callDuration: TextView
        var callDirection: ImageView

        init {
            with (itemView) {
                callerNumber = findViewById(R.id.callNumber)
                callDuration = findViewById(R.id.duration)
                this@Holder.dateTime = findViewById(R.id.dateTime)
                callDirection = findViewById(R.id.callDirection)
            }
        }
    }
}