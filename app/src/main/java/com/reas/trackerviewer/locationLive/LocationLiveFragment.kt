package com.reas.trackerviewer.locationLive

import android.os.Build
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

class LocationLiveFragment : Fragment(), OnMapReadyCallback {

    private val mMarkers: HashMap<String, Marker> = HashMap()
    private lateinit var mMap: GoogleMap

//    private val callback = OnMapReadyCallback { googleMap ->
//        /**
//         * Manipulates the map once available.
//         * This callback is triggered when the map is ready to be used.
//         * This is where we can add markers or lines, add listeners or move the camera.
//         * In this case, we just add a marker near Sydney, Australia.
//         * If Google Play services is not installed on the device, the user will be prompted to
//         * install it inside the SupportMapFragment. This method will only be triggered once the
//         * user has installed Google Play services and returned to the app.
//         */
//        val sydney = LatLng(-34.0, 151.0)
//
//        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
//        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
//    }

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
        mapFragment?.getMapAsync(this)
    }

    private fun getUpdates() {
        val ref = FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().uid}/location/${Build.DEVICE}")

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
        Log.d(TAG, "setMarker: ${locationObject.toString()}")
//        val value: HashMap<String, Any> = dataSnapshot.value as HashMap<String, Any>
//        val value = HashMap<String, Any>()
//        val value = dataSnapshot.value as HashMap<String, Any>

//        val value = mapOf(Pair(dataSnapshot.key, dataSnapshot.value))
//        Log.d(TAG, "setMarker: KEY: ${dataSnapshot.key}")
//
//        Log.d(TAG, "setMarker: VALUE: ${dataSnapshot.value}")

//        value.forEach {
//            Log.d(TAG, "setMarker: ${it.key} , ${it.value}")
//        }
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

    override fun onMapReady(googleMap: GoogleMap?) {
        mMap = googleMap!!
        mMap.setMaxZoomPreference(16F)
        getUpdates()
    }

    private fun loadJson(string: String): CustomLocationObject? {
        // Loads JSON File to CustomLocationObject
        Log.d(TAG, "loadJson: $string")
        val type = object : TypeToken<CustomLocationObject>() {}.type
        val ans = Gson().fromJson<CustomLocationObject>(string, type)
        Log.d(TAG, "loadJson: $ans")
        return ans
    }
}