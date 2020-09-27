package com.reas.trackerviewer.messages.chat

import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.reas.trackerviewer.messages.MessagesBaseObject
import java.io.BufferedReader
import java.io.File
import java.io.FileReader
import java.util.*
import kotlin.collections.HashMap

private const val TAG = "ChatHolder"

class ChatHolder {
    private var data = HashMap<String, ArrayList<MessagesBaseObject>>()

    fun setData(file: File) {
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
                data = Gson().fromJson(response, type)
            } catch (e: Exception) {
                Log.e(TAG, "loadSMS: Error", e)
            }
        }
    }

    fun getData(): HashMap<String, ArrayList<MessagesBaseObject>> = data


    companion object {
        private val holder = ChatHolder()

        fun getInstance(): ChatHolder = holder
    }
}