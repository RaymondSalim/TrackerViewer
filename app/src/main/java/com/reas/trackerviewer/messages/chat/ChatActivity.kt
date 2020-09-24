package com.reas.trackerviewer.messages.chat

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.reas.trackerviewer.R
import com.reas.trackerviewer.messages.MessagesBaseObject
import com.reas.trackerviewer.messages.MessagesViewModel

class ChatActivity : AppCompatActivity() {
    private var msgFrom: String? = null

    private val messagesViewModel: MessagesViewModel by lazy {
        ViewModelProvider(this).get(MessagesViewModel::class.java)
    }
    var data: HashMap<String, ArrayList<MessagesBaseObject>>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        msgFrom = intent.extras?.get("msgFrom") as String?
        data = messagesViewModel.getSMS()
        initializeRecyclerView()

    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }

    private fun initializeRecyclerView() {
        val recyclerView = findViewById<RecyclerView>(R.id.chatRV)
        val recyclerViewAdapter = ChatRecyclerViewAdapter(this, data!![msgFrom]!!)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        recyclerView.adapter = recyclerViewAdapter
        recyclerView.layoutManager = linearLayoutManager
    }
}