package com.reas.trackerviewer.messages

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*
import kotlin.collections.HashMap

private const val TAG = "MessagesViewModel"

class MessagesViewModel(application: Application): AndroidViewModel(application) {
    private val smsFile = File(application.filesDir.toString() + "/SMS.json")
    private val convFile = File(application.filesDir.toString() + "/Conversation.json")

//    private val smsMap: MutableLiveData<HashMap<String, ArrayList<MessagesBaseObject>>> by lazy {
//        val liveData = MutableLiveData<HashMap<String, ArrayList<MessagesBaseObject>>>()
//        while (!smsFile.exists()) {}
//        liveData.value = loadSMS(smsFile)
//        return@lazy liveData
//    }
//
//    private val convMap: MutableLiveData<SortedMap<String, MessagesBaseObject>> by lazy {
//        val liveData = MutableLiveData<SortedMap<String, MessagesBaseObject>>()
//        liveData.value = loadConversation(convFile)
//        return@lazy liveData
//    }

    private var smsMap = HashMap<String, ArrayList<MessagesBaseObject>>()
    private val convMap = MutableLiveData<SortedMap<String, MessagesBaseObject>>()

    private var convDownloaded = false
    private var messagesDownloaded = false

    fun smsFileDownloaded() {
        smsMap = loadSMS(smsFile)
        messagesDownloaded = true
    }

    fun convFileDownloaded() {
        convMap.value = loadConversation(convFile)
        convDownloaded = true
    }

    fun smsFileReady(): Boolean = messagesDownloaded

    fun convFileReady(): Boolean = convDownloaded

    fun fileReady(): Boolean = convDownloaded && messagesDownloaded

    fun getSMS(): HashMap<String, ArrayList<MessagesBaseObject>>? = smsMap

    fun getConv(): SortedMap<String, MessagesBaseObject>? = convMap.value

    private fun loadSMS(file: File): HashMap<String, ArrayList<MessagesBaseObject>> {
        var temp = java.util.HashMap<String, ArrayList<MessagesBaseObject>>()

        val fileReader = FileReader(file)
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
            val type = object : TypeToken<java.util.HashMap<String, ArrayList<MessagesBaseObject>>>() {}.type
            try {
                temp = Gson().fromJson(response, type)
            } catch (e: Exception) {
                Log.e(TAG, "loadSMS: Error", e)
            }
        }
        return temp
    }

    private fun loadConversation(file: File): SortedMap<String, MessagesBaseObject>? {
        var temp: HashMap<String, MessagesBaseObject>? = null
        var sorted: SortedMap<String, MessagesBaseObject>? = null

        val fileReader = FileReader(file)
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
            val type = object : TypeToken<HashMap<String, MessagesBaseObject>>() {}.type
            try {
                temp = Gson().fromJson(response, type)
            } catch (e: Exception) {
                Log.e(TAG, "loadConversation: Error", e)
            }
            sorted = sortHashMap(temp!!)
        }
        return sorted
    }

    fun dataChanged() {
        smsMap = loadSMS(smsFile)
        convMap.value = loadConversation(convFile)
    }

    private fun sortHashMap(hashMap: HashMap<String, MessagesBaseObject>): SortedMap<String, MessagesBaseObject> {
        val output: HashMap<String, MessagesBaseObject> = HashMap()
        hashMap.forEach {
            output[it.key] = it.value
        }

        return output.toSortedMap(compareByDescending { output[it]?.mTime })
    }

}