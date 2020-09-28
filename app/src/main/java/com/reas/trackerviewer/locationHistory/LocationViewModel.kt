package com.reas.trackerviewer.locationHistory

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

private const val TAG = "LocationViewModel"

class LocationViewModel(application: Application): AndroidViewModel(application) {
    private val locationFile = File(application.filesDir.toString() + "/Location.json")

    private var fileReady = false

//    private val list: MutableLiveData<ArrayList<LocationBaseObject>> by lazy {
//        val liveData = MutableLiveData<ArrayList<LocationBaseObject>>()
//        liveData.value = loadJson(file)
//        return@lazy liveData
//    }

    private val locationList = MutableLiveData<ArrayList<LocationBaseObject>>()

    private fun loadJson(locationFile: File): ArrayList<LocationBaseObject> {
        // Loads JSON File to ArrayList<LocationBaseObject>
        var temp = ArrayList<LocationBaseObject>()

        val fileReader = FileReader(locationFile)
        val bufferedReader = BufferedReader(fileReader)
        val stringBuilder = StringBuilder()
        var line = bufferedReader.readLine()
        while (line != null) {
            stringBuilder.append(line).append("\n")
            line = bufferedReader.readLine()
        }
        bufferedReader.close()
        val response = stringBuilder.toString()

        if (response != "") {
            val type = object : TypeToken<ArrayList<LocationBaseObject>>() {}.type
            try {
                temp = Gson().fromJson(response, type)
            } catch (e: Exception) {
                Log.e(TAG, "loadJson: Error", e)
            }
        }
        return temp
    }

    fun fileReady() {
        fileReady = true
    }

    fun dataChanged() {
        locationList.value = loadJson(locationFile)
    }

    fun filterList(time: Long): ArrayList<LocationBaseObject> {
        return locationList.value?.filter {
            it.mTime >= time && it.mTime < (time + 86400000)
        } as ArrayList<LocationBaseObject>
    }
}