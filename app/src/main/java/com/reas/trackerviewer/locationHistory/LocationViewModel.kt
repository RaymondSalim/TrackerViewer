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
    private val file = File(application.filesDir.toString() + "/Location.json")

    private val list: MutableLiveData<ArrayList<LocationBaseObject>> by lazy {
        val liveData = MutableLiveData<ArrayList<LocationBaseObject>>()
        liveData.value = loadJson(file)
        return@lazy liveData

    }

    fun getLocations(): MutableLiveData<ArrayList<LocationBaseObject>> = list

    private fun loadJson(locationFile: File): ArrayList<LocationBaseObject> {
        // Loads JSON File to ArrayList<SMSObject>
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
                temp = Gson().fromJson<ArrayList<LocationBaseObject>>(response, type)
            } catch (e: Exception) {
                Log.e(TAG, "loadJson: Error", e)
            }
        }
        return temp
    }

    fun dataChanged() {
        list.value = loadJson(file)
    }
}