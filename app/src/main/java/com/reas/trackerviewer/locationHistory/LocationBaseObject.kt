package com.reas.trackerviewer.locationHistory

data class LocationBaseObject(
     var mLatitude: Double = 0.0,
     var mLongitude: Double = 0.0,
     var mAccuracy: Float = 0F,
     var mSpeed: Float = 0F,
     var mTime: Long = 0L,
     var mProvider: String = "") {
}