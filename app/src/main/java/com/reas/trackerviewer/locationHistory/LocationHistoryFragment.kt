package com.reas.trackerviewer.locationHistory

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CalendarView
import android.widget.ImageButton
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageException
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.reas.trackerviewer.R
import java.io.File
import kotlin.math.roundToInt


private const val TAG = "LocationHistoryFragment"

class LocationHistoryFragment : Fragment() {
    private val auth = FirebaseAuth.getInstance()

    private val storage = Firebase.storage
    private val storageRef = storage.reference
    private lateinit var locationJsonRef: StorageReference

    private var mSwipeRefreshLayout: SwipeRefreshLayout? = null
    private var sheetBehavior: BottomSheetBehavior<ConstraintLayout>? = null

    private lateinit var locationFile: File
    private val locationViewModel: LocationViewModel by lazy {
        ViewModelProvider(this).get(LocationViewModel::class.java)
    }

    private val deviceID: String by lazy {
        val id = context?.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)?.getString(
            "activeDevice",
            ""
        ) ?: ""
        return@lazy id.substring(id.indexOf("(") + 1, id.indexOf(")"))
    }

    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap
        val sydney = LatLng(-34.0, 151.0)
        googleMap.setPadding(0,0,0, resources.getDimension(R.dimen.bottom_sheet_collapsed).toInt())
        googleMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(sydney))
    }

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationJsonRef = storageRef.child("users/${auth.uid}/${deviceID}/Location.json")
        locationFile = File(requireContext().filesDir.toString() + "/Location.json")

        downloadFile()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_location, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
        initializeBottomSheet()
    }

    private fun downloadFile() {
        locationJsonRef.getFile(locationFile).addOnSuccessListener {
            Log.d(TAG, "getData: Location File downloaded successfully")
            locationViewModel.dataChanged()

//            setPoints()

        }.addOnFailureListener {
            Log.d(TAG, "getData: Location File failed to download")

            val errorCode = (it as StorageException).errorCode
            if (errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) {
                Toast.makeText(context, "File failed to load please retry", Toast.LENGTH_SHORT).show()
            }
            mSwipeRefreshLayout?.isRefreshing = false
        }
    }

    private fun initializeBottomSheet() {
        val bottomSheet = view!!.findViewById<ConstraintLayout>(R.id.bottom_sheet)

        bottomSheet

        with(bottomSheet) {
            val calendarLayout = this.findViewById<ConstraintLayout>(R.id.calendarConstraintLayout)
            val expandButton = this.findViewById<ImageButton>(R.id.expandButton)
            val calendarView = this.findViewById<CalendarView>(R.id.calendarView)

            findViewById<Button>(R.id.header).setOnClickListener {
                toggleBottomSheet()
            }

            findViewById<ConstraintLayout>(R.id.baseConstraintLayout).setOnClickListener {
                if (calendarLayout.visibility == View.GONE) {
                    calendarLayout.visibility = View.VISIBLE
                    expandButton.setImageResource(R.drawable.ic_baseline_expand_less_24)

                } else {
                    calendarLayout.visibility = View.GONE
                    expandButton.setImageResource(R.drawable.ic_baseline_expand_more_24)

                }
            }

            calendarView.setOnDateChangeListener { view, year, month, dayOfMonth ->
                val newDate = "$dayOfMonth $month $year"

            }
        }

        sheetBehavior = BottomSheetBehavior.from(bottomSheet)

        // Solution from https://stackoverflow.com/a/52815006/12201419
        sheetBehavior?.addBottomSheetCallback(object : BottomSheetBehavior.BottomSheetCallback() {
            override fun onStateChanged(bottomSheet: View, newState: Int) {
            }

            override fun onSlide(bottomSheet: View, slideOffset: Float) {
//                mMap.setPadding(0, slideOffset.toInt(), 0 , 0)
//                mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(-34.0, 151.0)))
                when (sheetBehavior!!.state) {
                    BottomSheetBehavior.STATE_DRAGGING -> {
                        setMapPaddingBottom(slideOffset)
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(-34.0, 151.0)))
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                        setMapPaddingBottom(slideOffset)
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(LatLng(-34.0, 151.0)))
                    }
                    else -> {}
                }
            }

            fun setMapPaddingBottom(offset: Float) {
                val maxMapPaddingBottom = resources.getDimension(R.dimen.bottom_sheet_expanded) - (resources.getDimension(R.dimen.bottom_sheet_collapsed) / 2) //Multiplied by 2 because the initial padding is the same
                mMap.setPadding(0, 0, 0, ((offset * maxMapPaddingBottom).roundToInt()))
            }
        })
    }

    private fun toggleBottomSheet() {
        if (sheetBehavior!!.getState() !== BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior!!.setState(BottomSheetBehavior.STATE_EXPANDED)
        } else {
            sheetBehavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED)
        }
    }
}