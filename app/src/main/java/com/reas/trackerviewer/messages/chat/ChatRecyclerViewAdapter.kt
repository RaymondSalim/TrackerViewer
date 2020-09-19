package com.reas.trackerviewer.messages.chat

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.reas.trackerviewer.R
import com.reas.trackerviewer.messages.MessagesBaseObject
import com.reas.trackerviewer.messages.MessagesRecyclerView
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

class ChatRecyclerViewAdapter(
    private val context: Context,
    private val data: ArrayList<MessagesBaseObject>
): RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    private val layoutInflater = LayoutInflater.from(context)

    companion object {
        private const val VIEW_TYPE_MESSAGE_SENT = 1
        private const val VIEW_TYPE_MESSAGE_RECEIVED = 2
    }

    override fun getItemViewType(position: Int): Int {
        val messagesBaseObject = data[position]

        when (messagesBaseObject.mDirection) {
            "Incoming" -> {
                return VIEW_TYPE_MESSAGE_RECEIVED
            }

            "Outgoing" -> {
                return VIEW_TYPE_MESSAGE_SENT
            }
        }

        return super.getItemViewType(position)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        var view: View? = null

        if (viewType == VIEW_TYPE_MESSAGE_RECEIVED) {
            view = layoutInflater.inflate(R.layout.recyclerview_message_received, parent, false)
            return ReceivedHolder(view)
        } else if (viewType == VIEW_TYPE_MESSAGE_SENT) {
            view = layoutInflater.inflate(R.layout.recyclerview_message_sent, parent, false)
            return SentHolder(view)
        }
        return SentHolder(view!!)
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val messagesBaseObject = data[position]

        when (holder.itemViewType) {
            VIEW_TYPE_MESSAGE_SENT -> {
                (holder as SentHolder).bind(messagesBaseObject)
            }

            VIEW_TYPE_MESSAGE_RECEIVED -> {
                (holder as ReceivedHolder).bind(messagesBaseObject)
            }
        }
    }

    override fun getItemCount(): Int = data.size

    class ReceivedHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val textMessageBody: TextView = itemView.findViewById(R.id.textMessageBody)
        val textMessageTime: TextView = itemView.findViewById(R.id.textMessageTime)

        fun bind(messagesBaseObject: MessagesBaseObject) {
            val calender = Calendar.getInstance()

            calender.timeZone = TimeZone.getDefault()
            calender.timeInMillis = messagesBaseObject.mTime

            val timeFormat = SimpleDateFormat("HH:mm")

            textMessageBody.text = messagesBaseObject.mMessage
            textMessageTime.text = timeFormat.format(calender.time)
        }
    }

    class SentHolder(itemView: View): RecyclerView.ViewHolder(itemView) {
        val textMessageBody: TextView = itemView.findViewById(R.id.textMessageBody)
        val textMessageTime: TextView = itemView.findViewById(R.id.textMessageTime)

        fun bind(messagesBaseObject: MessagesBaseObject) {
            val calender = Calendar.getInstance()

            calender.timeZone = TimeZone.getDefault()
            calender.timeInMillis = messagesBaseObject.mTime

            val timeFormat = SimpleDateFormat("HH:mm")

            textMessageBody.text = messagesBaseObject.mMessage
            textMessageTime.text = timeFormat.format(calender.time)
        }
    }
}