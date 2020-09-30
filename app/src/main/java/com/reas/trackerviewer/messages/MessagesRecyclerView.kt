package com.reas.trackerviewer.messages

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.reas.trackerviewer.MainActivity
import com.reas.trackerviewer.R
import com.reas.trackerviewer.messages.chat.ChatActivity
import java.io.File
import java.text.SimpleDateFormat
import java.util.*

private const val TAG = "MessagesRecyclerView"

class MessagesRecyclerView(
    private val context: Context,
    private val list: SortedMap<String, MessagesBaseObject>
): RecyclerView.Adapter<MessagesRecyclerView.ViewHolder>() {
    private val layoutInflater = LayoutInflater.from(context)
    
    private val smsFile = File(context.filesDir.toString() + "/SMS.json")

    private var fileReady = false

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(layoutInflater.inflate(R.layout.recyclerview_sms, null, false))
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val keyList = ArrayList(list.keys)
        val currentMessageObject = list[keyList[position]]

        holder.smsSender.text = keyList[position]
        holder.summarySMS.text = currentMessageObject?.mMessage

        val calender = Calendar.getInstance()
        calender.timeZone = TimeZone.getDefault()
        calender.timeInMillis = currentMessageObject!!.mTime

        val dateFormat = SimpleDateFormat("dd/MM/yyyy")
        val timeFormat = SimpleDateFormat("HH:mm")
        timeFormat.timeZone = TimeZone.getDefault()
        dateFormat.timeZone = TimeZone.getDefault()

        holder.date.text = dateFormat.format(calender.time)
        holder.time.text = timeFormat.format(calender.time)

        val test = context as MainActivity
        val frag = test.supportFragmentManager.findFragmentByTag("messagesFragment") as MessagesFragment

        holder.itemView.setOnClickListener {
            if (frag.smsFileDownloaded()) {
                val chatIntent = Intent(context, ChatActivity::class.java)
                chatIntent.putExtra("msgFrom", keyList[position])
                context.startActivity(chatIntent)
            } else {
                Toast.makeText(context, "File is still downloading, please wait", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun getItemCount(): Int = list.keys.size

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        var summarySMS: TextView
        var date: TextView
        var time: TextView
        var smsSender: TextView
        var context: Context? = null

        init {
            with(itemView) {
                summarySMS = findViewById(R.id.smsSummary)
                date = findViewById(R.id.date)
                time = findViewById(R.id.time)
                smsSender = findViewById(R.id.smsSender)
            }

        }
    }
}