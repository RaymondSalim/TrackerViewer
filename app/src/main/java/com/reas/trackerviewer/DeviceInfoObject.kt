package com.reas.trackerviewer

data class DeviceInfoObject(
    val device: String = "",
    val fingerprint: String = "",
    val manufacturer: String = "",
    val model: String = "") {
}

fun DeviceInfoObject() {}