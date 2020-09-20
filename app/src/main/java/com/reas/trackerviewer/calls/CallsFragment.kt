package com.reas.trackerviewer.calls

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.ktx.storage
import com.reas.trackerviewer.R
import java.io.File

class CallsFragment : Fragment(), SwipeRefreshLayout.OnRefreshListener {
    private val auth = FirebaseAuth.getInstance()

    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null

    private val storage = Firebase.storage
    private val storageRef = storage.reference

//    val locationJsonRef: StorageReference = storageRef.child("users/${auth.uid}/${Build.ID}/Location.json")
    private val callJsonRef = storageRef.child("users/${auth.uid}/${Build.ID}/Calls.json")

    private lateinit var root: View

    private lateinit var callFile: File

    var callData: HashMap<String, ArrayList<CallBaseObject>>? = null

    private val callViewModel: CallViewModel by lazy {
        ViewModelProvider(this).get(CallViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        callFile = File(requireContext().filesDir.toString() + "/Calls.json")

        getData()



    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        root = inflater.inflate(R.layout.fragment_calls, container, false)



        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeSwipeLayout()
    }

    private fun getData() {
        callJsonRef.getFile(callFile).addOnSuccessListener {
            Log.d("CallsFragment", "getData: Call File Downloaded")
            callViewModel.dataChanged()
            callData = callViewModel.getMap()
            initializeRecyclerView()

        }.addOnFailureListener {
            Log.d("CallsFragment", "getData: Call File Failed to Download")
        }

    }

    private fun initializeRecyclerView() {
        var recyclerView: RecyclerView
        var recyclerViewAdapter: CallRecyclerView? = null
        if (callData != null) {
            recyclerView = root.findViewById(R.id.callRV)
            recyclerViewAdapter = CallRecyclerView(requireActivity(), callData!!)
            recyclerView.adapter = recyclerViewAdapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
        } else {
            recyclerViewAdapter?.notifyDataSetChanged()
        }

        mSwipeRefreshLayout?.isRefreshing = false
    }

    private fun initializeSwipeLayout() {
        mSwipeRefreshLayout = view?.findViewById(R.id.calls_container)
        mSwipeRefreshLayout?.setOnRefreshListener(this)
        mSwipeRefreshLayout?.setColorSchemeColors(
            R.color.colorPrimary,
            android.R.color.holo_green_dark,
            android.R.color.holo_orange_dark,
            android.R.color.holo_blue_dark)

        mSwipeRefreshLayout?.post {
            Log.d("TEST", "initializeSwipeLayout: runnable")
            mSwipeRefreshLayout?.isRefreshing = true

            getData()
        }
    }

    override fun onRefresh() {
        Log.d("TEST", "onRefresh: called")
        getData()
    }

}