package com.reas.trackerviewer.ussd

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.io.FileReader

class USSDViewModel(application: Application): AndroidViewModel(application) {
    private val file = File(application.filesDir.toString() + "/USSD.json")

    private val ussdMap: MutableLiveData<HashMap<String, String>> by lazy {
        val liveData = MutableLiveData<HashMap<String, String>>()
        liveData.value = loadJson(file)
        return@lazy liveData
    }

    private fun loadJson(jsonFile: File): HashMap<String, String> {
        var temp = HashMap<String, String>()

        // Loads JSON File to HashMap<String, String>
        val fileReader = FileReader(jsonFile)
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
            val type = object : TypeToken<HashMap<String, String>>() {}.type
            temp = Gson().fromJson<HashMap<String, String>>(response, type)
        }
        return temp
    }

    fun dataChanged() {
        ussdMap.value = loadJson(file)
    }

    fun getMap(): HashMap<String, String>? = ussdMap.value

}