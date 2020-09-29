package com.reas.trackerviewer.messages.chat

import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.reas.trackerviewer.R
import com.reas.trackerviewer.messages.MessagesBaseObject

class ChatActivity : AppCompatActivity() {
    private var msgFrom: String? = null

//    private val messagesViewModel: MessagesViewModel by lazy {
//        ViewModelProvider(this).get(MessagesViewModel::class.java)
//    }
    private lateinit var data: HashMap<String, ArrayList<MessagesBaseObject>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat)

        msgFrom = intent.extras?.get("msgFrom") as String?

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.chat_toolbar)
        toolbar.title = msgFrom
        setSupportActionBar(toolbar!!)
        supportActionBar!!.setHomeButtonEnabled(true)
        supportActionBar!!.setDisplayHomeAsUpEnabled(true)

        initializeRecyclerView()

    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return true
    }

    private fun initializeRecyclerView() {
        data = ChatHolder.getInstance().getData()
        val recyclerView = findViewById<RecyclerView>(R.id.chatRV)
        val recyclerViewAdapter = ChatRecyclerViewAdapter(this, data!![msgFrom]!!)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.stackFromEnd = true
        recyclerView.adapter = recyclerViewAdapter
        recyclerView.layoutManager = linearLayoutManager
    }
}