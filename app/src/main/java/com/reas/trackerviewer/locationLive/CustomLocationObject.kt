package com.reas.trackerviewer.locationLive

import android.os.Bundle

data class CustomLocationObject(
    val altitude: Double,
    val speedAccuracyMetersPerSecond: Float,
    val bearing: Float,
    val latitude: Double,
    val accuracy: Float,
    val bearingAccuracyDegrees: Float,
    val extras: Bundle,
    val elapsedRealtimeNanos: Long,
    val speed: Float,
    val provider: String,
    val elapsedRealtimeUncertaintyNanos: Double,
    val fromMockProvider: Boolean,
    val time: Long,
    val complete: Boolean,
    val longitude: Double,
    val verticalAccuracyMeters: Float
) {
}