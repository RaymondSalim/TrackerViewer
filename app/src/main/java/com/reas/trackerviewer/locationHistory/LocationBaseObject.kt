package com.reas.trackerviewer.locationHistory

import android.location.Location

data class LocationBaseObject(val location: Location) {
     var mLatitude: Double = location.latitude
     var mLongitude: Double = location.longitude
     var mAccuracy: Float = location.accuracy
     var mSpeed: Float = location.speed
     var mTime: Long = location.time
     var mProvider = location.provider
}