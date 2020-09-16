package com.reas.trackerviewer.calls

import android.os.Build
import android.os.Bundle
import android.telecom.Call
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.reas.trackerviewer.R
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.lang.StringBuilder

class CallsFragment : Fragment() {
    private val auth = FirebaseAuth.getInstance()

    private val storage = Firebase.storage
    private val storageRef = storage.reference

    val smsJsonRef: StorageReference = storageRef.child("users/${auth.uid}/${Build.ID}/SMS.json")
    val ussdJsonRef: StorageReference = storageRef.child("users/${auth.uid}/${Build.ID}/USSD.json")
    val locationJsonRef: StorageReference = storageRef.child("users/${auth.uid}/${Build.ID}/Location.json")
    val callJsonRef = storageRef.child("users/${auth.uid}/${Build.ID}/Calls.json")

    private lateinit var root: View


    private lateinit var callFile: File

    var callData: HashMap<String, ArrayList<CallBaseObject>>? = null
    

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

    private fun getData() {
        var temp: HashMap<String, ArrayList<CallBaseObject>>? = null
        callJsonRef.getFile(callFile).addOnSuccessListener {
            Log.d("CallsFragment", "getData: Call File Downloaded")
            callData = loadFile()

            initializeRecyclerView()

        }.addOnFailureListener {
            Log.d("CallsFragment", "getData: Call File Failed to Download")
        }

    }

    private fun loadFile(): HashMap<String, ArrayList<CallBaseObject>> {
        var temp = HashMap<String, ArrayList<CallBaseObject>>()

        val fileReader = FileReader(callFile)
        val bufferedReader = BufferedReader(fileReader)
        val stringBuilder  = StringBuilder()
        var line = bufferedReader.readLine()
        while (line != null) {
            stringBuilder.append(line).append("\n")
            line = bufferedReader.readLine()
        }
        bufferedReader.close()
        val response = stringBuilder.toString()

        if (response != "") {
            val type = object : TypeToken<HashMap<String, ArrayList<CallBaseObject>>>() {}.type
            temp = Gson().fromJson(response, type)
        }

        return temp
    }

    private fun initializeRecyclerView() {
        if (callData != null) {
            val recyclerView = root.findViewById<RecyclerView>(R.id.callRV)
            val recyclerViewAdapter = CallRecyclerView(requireActivity(), callData!!)
            recyclerView.adapter = recyclerViewAdapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
        }
    }

}