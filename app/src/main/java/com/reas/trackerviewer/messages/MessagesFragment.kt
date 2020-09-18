package com.reas.trackerviewer.messages

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.reas.trackerviewer.R
import java.io.File

private const val TAG = "MessagesFragment"

class MessagesFragment : Fragment() {
    private val auth = FirebaseAuth.getInstance()
    private val storage = Firebase.storage
    private val storageRef = storage.reference

    val smsJsonRef: StorageReference = storageRef.child("users/${auth.uid}/${Build.ID}/SMS.json")
    val convJsonRef: StorageReference = storageRef.child("users/${auth.uid}/${Build.ID}/Conversation.json")

    private lateinit var smsFile: File
    private lateinit var convFile: File

    private val messagesViewModel: MessagesViewModel by lazy {
        ViewModelProvider(this).get(MessagesViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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
        initializeRecyclerView()
    }

    private fun getData() {
        smsJsonRef.getFile(smsFile).addOnSuccessListener {
            Log.d(TAG, "getData: SMS File downloaded")
        }.addOnFailureListener {
            Log.e(TAG, "getData: SMS File failed to download", it)
            Log.d(TAG, "getData: SMS File failed to download. Retrying once")
            smsJsonRef.getFile(smsFile)
        }

        convJsonRef.getFile(convFile).addOnSuccessListener {
            Log.d(TAG, "getData: Conversation File downloaded")
            initializeRecyclerView()

        }.addOnFailureListener {
            Log.e(TAG, "getData: Conversation File failed to download", it)
            Log.d(TAG, "getData: Conversation File failed to download. Retrying once")
            convJsonRef.getFile(convFile)
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

    }

}