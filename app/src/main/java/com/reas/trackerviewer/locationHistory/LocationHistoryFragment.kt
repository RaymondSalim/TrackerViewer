package com.reas.trackerviewer.locationHistory

import android.animation.LayoutTransition
import android.content.Context
import android.icu.util.Calendar
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.roundToInt


private const val TAG = "LocationHistoryFragment"

class LocationHistoryFragment : Fragment() {
    private val auth = FirebaseAuth.getInstance()

    private val storage = Firebase.storage
    private val storageRef = storage.reference
    private lateinit var locationJsonRef: StorageReference

    private var sheetBehavior: BottomSheetBehavior<ConstraintLayout>? = null
    private var linearLayoutDownload: LinearLayout? = null
    private var emptyTextView: TextView? = null
    private var dateTextView: TextView? = null
    private var progressText: TextView? = null
    private var recyclerView: RecyclerView? = null
    private var progressBar: ProgressBar? = null
    private var calendarView: CalendarView? = null
    private var baseView: View? = null

    private var localFile = false

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

    private var today: Long = 0

    private var lastLocation: LatLng? = LatLng(0.0, 0.0)

    private val callback = OnMapReadyCallback { googleMap ->
        mMap = googleMap

        // Prevents map moving to marker when sliding bottomsheetdialog
        mMap.setOnCameraMoveListener { setLastLocation(mMap.cameraPosition.target) }

        if (localFile) {
            mMap?.addMarker(MarkerOptions().position(lastLocation!!))
            mMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 17F))
        }
    }

    private lateinit var mMap: GoogleMap

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        locationJsonRef = storageRef.child("users/${auth.uid}/${deviceID}/Location.json")
        locationFile = File(requireContext().filesDir.toString() + "/Location.json")

        // Finds the current date
        getTime()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        baseView = inflater.inflate(R.layout.fragment_location, container, false)

        emptyTextView = baseView!!.findViewById(R.id.empty)
        recyclerView = baseView!!.findViewById(R.id.locationRecyclerView)
        progressBar = baseView!!.findViewById(R.id.progressBar)
        progressText = baseView!!.findViewById(R.id.progressText)
        calendarView = baseView!!.findViewById(R.id.calendarView)
        dateTextView = baseView!!.findViewById(R.id.date)
        linearLayoutDownload = baseView!!.findViewById(R.id.downloadingLayout)
        
        dateTextView!!.text = "Today"
        calendarView!!.maxDate = today

        if (locationFile.exists()) {
            localFile = true

            locationViewModel.setFileReady()
            locationViewModel.dataChanged()

            linearLayoutDownload?.visibility = View.GONE

            initializeBottomSheet()


            lastLocation = locationViewModel.lastLocation()
        } else {
            downloadFile()
        }
        
        return baseView
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)



    }

    private fun downloadFile() {
        progressBar?.isIndeterminate = false

        locationJsonRef.getFile(locationFile).addOnSuccessListener {
            Log.d(TAG, "getData: Location File downloaded successfully")
            locationViewModel.setFileReady()
            locationViewModel.dataChanged()

            linearLayoutDownload?.visibility = View.GONE

            initializeBottomSheet()


            lastLocation = locationViewModel.lastLocation()
            mMap.addMarker(MarkerOptions().position(lastLocation!!))
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastLocation, 15F))

        }.addOnFailureListener {
            Log.d(TAG, "getData: Location File failed to download")

            val errorCode = (it as StorageException).errorCode
            if (errorCode != StorageException.ERROR_OBJECT_NOT_FOUND) {
                Toast.makeText(context, "File failed to load please retry", Toast.LENGTH_SHORT).show()
            }
        }.addOnProgressListener {
            val fileSize = it.totalByteCount.toFloat()
            val progress = it.bytesTransferred.toFloat()
            progressBar!!.progress = (100 * (progress/fileSize)).toInt()
        }
    }

    private fun initializeBottomSheet() {
        val bottomSheet = baseView!!.findViewById<ConstraintLayout>(R.id.bottom_sheet)

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
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(lastLocation))
                    }
                    BottomSheetBehavior.STATE_SETTLING -> {
                        setMapPaddingBottom(slideOffset)
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(lastLocation))
                    }
                    else -> {}
                }
            }

            fun setMapPaddingBottom(offset: Float) {
                val maxMapPaddingBottom = resources.getDimension(R.dimen.bottom_sheet_expanded) - (resources.getDimension(R.dimen.bottom_sheet_collapsed) / 2) //Multiplied by 2 because the initial padding is the same
                mMap.setPadding(0, 0, 0, ((offset * maxMapPaddingBottom).roundToInt()))
            }
        })


        val calendarLayout = baseView!!.findViewById<ConstraintLayout>(R.id.calendarConstraintLayout)
        val expandButton = baseView!!.findViewById<ImageButton>(R.id.expandButton)

        val linearLayout = baseView!!.findViewById<LinearLayout>(R.id.linearLayout)


        // See https://stackoverflow.com/a/39462475/12201419 why the transition is not set on the xml layout file
        val transition = LayoutTransition()
        transition.setAnimateParentHierarchy(false)
        linearLayout.layoutTransition = transition

        baseView!!.findViewById<Button>(R.id.header).setOnClickListener {
            toggleBottomSheet()
        }

        baseView!!.findViewById<ConstraintLayout>(R.id.baseConstraintLayout).setOnClickListener {
            if (calendarLayout.visibility == View.GONE) {
                    if (sheetBehavior!!.state == BottomSheetBehavior.STATE_COLLAPSED) {
                        sheetBehavior!!.state = BottomSheetBehavior.STATE_EXPANDED
                    }
                        calendarLayout.visibility = View.VISIBLE
                    expandButton.setImageResource(R.drawable.ic_baseline_expand_less_24)

                } else {
                    calendarLayout.visibility = View.GONE
                    expandButton.setImageResource(R.drawable.ic_baseline_expand_more_24)
                }
            }


        initializeRecyclerView(today)

        calendarView!!.setOnDateChangeListener { view, year, month, dayOfMonth ->
            // Closes the CalenderView
            calendarLayout.visibility = View.GONE

            val date = Calendar.getInstance()
            date.set(year, month, dayOfMonth, 0, 0, 0)

//               initializeBottomSheet() val filteredList = locationViewModel.filterList(date.timeInMillis)
//                if (filteredList.isEmpty()) {
//                    emptyTextView?.visibility = View.VISIBLE
//                    recyclerView?.visibility = View.GONE
//                } else {
//                    emptyTextView?.visibility = View.GONE
//                    recyclerView?.visibility = View.VISIBLE
//
//                    initializeRecyclerView(filteredList)
//                }

            initializeRecyclerView(date.timeInMillis)
//                initializeRecyclerView(1601398800772)


            // Ignores miliseconds
            if ((date.timeInMillis / 1000).toInt() >= (today/1000).toInt()) {
                dateTextView!!.text = "Today"
            } else {
                val format = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault())
                dateTextView!!.text = format.format(date.timeInMillis)
            }

        }
    }

    private fun initializeRecyclerView(time:Long) {
        progressBar?.isIndeterminate = true
        progressText?.text = getString(R.string.processing)
        val filteredList = locationViewModel.filterList(time)
        if (filteredList?.isEmpty() != false) {
            emptyTextView?.visibility = View.VISIBLE
            recyclerView?.visibility = View.GONE
        } else {
            emptyTextView?.visibility = View.GONE
            recyclerView?.visibility = View.VISIBLE

            var recyclerView = baseView!!.findViewById<RecyclerView>(R.id.locationRecyclerView)
            var adapter = LocationHistoryRecyclerView(requireContext(), filteredList)
            recyclerView.adapter = adapter
            recyclerView.layoutManager = LinearLayoutManager(requireContext())
        }
    }

    private fun toggleBottomSheet() {
        if (sheetBehavior!!.getState() !== BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehavior!!.setState(BottomSheetBehavior.STATE_EXPANDED)
        } else {
            sheetBehavior!!.setState(BottomSheetBehavior.STATE_COLLAPSED)
        }
    }

    private fun getTime() {
        val time = Date().time
        val year = SimpleDateFormat("yyyy")
        val month = SimpleDateFormat("MM")
        val day = SimpleDateFormat("dd")

        val calendar = Calendar.getInstance()
//        Log.d(TAG, "getTime: time $time")
//        Log.d(TAG, "getTime: year ${year.format(time).toInt()}")
//        Log.d(TAG, "getTime: month ${month.format(time).toInt()}")
//        Log.d(TAG, "getTime: day ${day.format(time).toInt()}")

        calendar.set(year.format(time).toInt(), month.format(time).toInt() - 1, day.format(time).toInt(), 0, 0, 0)


        today = calendar.timeInMillis
//        Log.d(TAG, "getTime: $today")
    }

    fun setMap(address: LocationGeocoderObject) {
        mMap.clear()
        mMap.addMarker(MarkerOptions().position(address.latLng).title(address.name))
//        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(address.latLng, 15F))
        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(address.latLng, 17F))
    }

    fun setLastLocation(latLng: LatLng) {
        lastLocation = latLng
    }
}