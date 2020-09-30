package com.reas.trackerviewer.ussd

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

class USSDFragment : Fragment() {
    private val auth = FirebaseAuth.getInstance()

    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null

    private val storage = Firebase.storage
    private val storageRef = storage.reference

    private lateinit var ussdJsonRef: StorageReference

    private lateinit var root: View

    private lateinit var ussdFile: File

    private var data: HashMap<String, String>? = null

    private val deviceID: String by lazy {
        val id = context?.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)?.getString("activeDevice", "") ?: ""
        return@lazy id.substring(id.indexOf("(")+1, id.indexOf(")"))
    }

    private val ussdViewModel: USSDViewModel by lazy {
        ViewModelProvider(this).get(USSDViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ussdJsonRef = storageRef.child("users/${auth.uid}/${deviceID}/USSD.json")

        ussdFile = File(requireContext().filesDir.toString() + "/USSD.json")

        if (ussdFile.exists()) {
            ussdViewModel.dataChanged()
            data = ussdViewModel.getMap()
            initializeRecyclerView()
        } else {
            getData()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_ussd, container, false)
        return root
    }

    private fun getData() {
        ussdJsonRef.getFile(ussdFile).addOnSuccessListener {
            Log.d("USSDFragment", "getData: USSD File Downloaded")
            ussdViewModel.dataChanged()
            data = ussdViewModel.getMap()
            initializeRecyclerView()

        }.addOnFailureListener {
            Log.d("USSDFragment", "getData: USSD File Failed to Download")

            val errorCode = (it as StorageException).errorCode
            if (errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) {
                Toast.makeText(context, "File failed to load please retry", Toast.LENGTH_SHORT).show()
            }
            mSwipeRefreshLayout?.isRefreshing = false
        }
    }

    private fun initializeRecyclerView() {
        var recyclerView: RecyclerView
        var recyclerViewAdapter: USSDRecyclerView? = null
        if (data != null) {
            recyclerView = root.findViewById(R.id.ussd_rv)
            recyclerViewAdapter = USSDRecyclerView(requireActivity(), data!!)
            recyclerView.adapter = recyclerViewAdapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
        } else {
            recyclerViewAdapter?.notifyDataSetChanged()
        }

        mSwipeRefreshLayout?.isRefreshing = false
    }



}