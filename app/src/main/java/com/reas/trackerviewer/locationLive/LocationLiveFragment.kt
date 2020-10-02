package com.reas.trackerviewer.locationLive

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.reas.trackerviewer.R

private const val TAG = "LocationLiveFragment"

class LocationLiveFragment : Fragment() {

    private val mMarkers: HashMap<String, Marker> = HashMap()
    private lateinit var mMap: GoogleMap

    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap!!
        mMap.setMaxZoomPreference(16F)
        getUpdates()
    }

    private val deviceID: String by lazy {
        val id = context?.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)?.getString("activeDevice", "") ?: ""
        return@lazy id.substring(id.indexOf("(")+1, id.indexOf(")"))
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_location_live, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun getUpdates() {
        val ref = FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().uid}/location/${deviceID}")

        ref.addValueEventListener(object : ValueEventListener {
            var location: CustomLocationObject? = null
            override fun onDataChange(snapshot: DataSnapshot) {
                location = loadJson(snapshot.value.toString())
                setMarker(location, snapshot)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e(TAG, "onCancelled: Failed to read database values", error.toException())
            }

        })
    }

    private fun setMarker(locationObject: CustomLocationObject?, snapshot: DataSnapshot) {
        val key = snapshot.key
//        val value: HashMap<String, Any> = dataSnapshot.value as HashMap<String, Any>
//        val value = HashMap<String, Any>()
//        val value = dataSnapshot.value as HashMap<String, Any>

//        val value = mapOf(Pair(dataSnapshot.key, dataSnapshot.value))
//

//        val lat = value["latitude"].toString().toDouble()
//        val lng = value["longitude"].toString().toDouble()
        val lat = locationObject?.latitude
        val lng = locationObject?.longitude



        if (lat != null && lng != null) {
            val location = LatLng(lat!!, lng!!)

            if (!mMarkers.containsKey(key)) {
                mMarkers[key!!] = mMap.addMarker(MarkerOptions().title(key).position(location))
            } else {
                mMarkers[key!!]?.position = location
            }

//            val builder = LatLngBounds.Builder()
//            for (marker in mMarkers.values) {
//                builder.include(marker.position)
//            }
//            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(builder.build(), 300))

            mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(LatLngBounds(location, location), 100, 100, 10))
            }


    }


    private fun loadJson(string: String): CustomLocationObject? {
        // Loads JSON File to CustomLocationObject
        val type = object : TypeToken<CustomLocationObject>() {}.type
        val ans = Gson().fromJson<CustomLocationObject>(string, type)
        return ans
    }
}