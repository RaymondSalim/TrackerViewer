package com.reas.trackerviewer.messages

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.reas.trackerviewer.R
import java.io.File

private const val TAG = "MessagesFragment"

class MessagesFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    private val auth = FirebaseAuth.getInstance()
    private val storage = Firebase.storage
    private val storageRef = storage.reference

    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null

    private lateinit var smsJsonRef: StorageReference
    private lateinit var convJsonRef: StorageReference

    private lateinit var smsFile: File
    private lateinit var convFile: File

    private val deviceID: String by lazy {
        val id = context?.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)?.getString("activeDevice", "") ?: ""
        return@lazy id.substring(id.indexOf("(")+1, id.indexOf(")"))
    }

    private val messagesViewModel: MessagesViewModel by lazy {
        ViewModelProvider(this).get(MessagesViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        Log.d(TAG, "onCreate: fragment Created")

        smsJsonRef = storageRef.child("users/${auth.uid}/${deviceID}/SMS.json")
        convJsonRef = storageRef.child("users/${auth.uid}/${deviceID}/Conversation.json")

        Log.d(TAG, "onCreate: ${"users/${auth.uid}/${deviceID}/SMS.json"}")

        smsFile = File(requireContext().filesDir.toString() + "/SMS.json")
        convFile = File(requireContext().filesDir.toString() + "/Conversation.json")

        getData()

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_messages, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeSwipeLayout()
    }

    private fun initializeSwipeLayout() {
        mSwipeRefreshLayout = view?.findViewById(R.id.messages_container)
        mSwipeRefreshLayout?.setOnRefreshListener(this)
        mSwipeRefreshLayout?.setColorSchemeColors(
                R.color.colorPrimaryLight,
                android.R.color.holo_green_dark,
                android.R.color.holo_orange_dark,
                android.R.color.holo_blue_dark)

        mSwipeRefreshLayout?.post {
            mSwipeRefreshLayout?.isRefreshing = true

            getData()
        }
    }

    private fun getData() {
        smsJsonRef.getFile(smsFile).addOnSuccessListener {
            Log.d(TAG, "getData: SMS File downloaded")
            // Initializes the data on MessagesViewModel
            messagesViewModel.smsFileDownloaded()

            if (messagesViewModel.fileReady()) {
                initializeRecyclerView()
            }

        }.addOnFailureListener {
            Log.e(TAG, "getData: SMS File failed to download", it)

            val errorCode = (it as StorageException).errorCode
            if (errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) {
                Toast.makeText(context, "File failed to load please retry", Toast.LENGTH_SHORT).show()
            }
            mSwipeRefreshLayout?.isRefreshing = false
        }

        convJsonRef.getFile(convFile).addOnSuccessListener {
            Log.d(TAG, "getData: Conversation File downloaded")
//            messagesViewModel.dataChanged()
            messagesViewModel.convFileDownloaded()

            if (messagesViewModel.fileReady()) {
                initializeRecyclerView()
            }

        }.addOnFailureListener {
            Log.e(TAG, "getData: Conversation File failed to download", it)

            val errorCode = (it as StorageException).errorCode
            if (errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) {
                Toast.makeText(context, "File failed to load please retry", Toast.LENGTH_SHORT).show()
            }
            mSwipeRefreshLayout?.isRefreshing = false
        }
    }

    private fun initializeRecyclerView() {
        val convData = messagesViewModel.getConv()

        var recyclerView: RecyclerView
        var recyclerViewAdapter: MessagesRecyclerView? = null

        if (convData != null) {
            recyclerView = view!!.findViewById(R.id.messagesRecyclerView)
            recyclerViewAdapter = MessagesRecyclerView(requireContext(), convData)
            recyclerView.adapter = recyclerViewAdapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
        }

        mSwipeRefreshLayout?.isRefreshing = false
    }

    override fun onRefresh() {
        getData()
    }

    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "onDetach: Destroyed")
    }
}