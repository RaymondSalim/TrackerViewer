package com.reas.trackerviewer.calls

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

private const val TAG = "CallViewModel"

class CallViewModel(application: Application) : AndroidViewModel(application) {
    private val file = File(application.filesDir.toString() + "/Calls.json")

    private val callMap: MutableLiveData<HashMap<String, ArrayList<CallBaseObject>>> by lazy {
        val liveData = MutableLiveData<HashMap<String, ArrayList<CallBaseObject>>>()
        liveData.value = loadJson(file)
        return@lazy liveData
        }

    fun dataChanged() {
        callMap.value = loadJson(file)
    }

    private fun loadJson(jsonFile: File): HashMap<String, ArrayList<CallBaseObject>>? {
        var temp = HashMap<String, ArrayList<CallBaseObject>>()

        val fileReader = FileReader(jsonFile)
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

    fun getMap(): HashMap<String, ArrayList<CallBaseObject>>? = callMap.value

}