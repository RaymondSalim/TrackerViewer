package com.reas.trackerviewer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.reas.trackerviewer.calls.CallsFragment
import com.reas.trackerviewer.locationHistory.LocationHistoryFragment
import com.reas.trackerviewer.locationLive.LocationLiveFragment
import com.reas.trackerviewer.messages.MessagesFragment
import com.reas.trackerviewer.ussd.USSDFragment
import kotlinx.android.synthetic.main.activity_main.*
import java.io.File

class MainActivity : AppCompatActivity() {
    private lateinit var mDrawer: DrawerLayout
    private lateinit var navDrawer: NavigationView

    private var messagesFragment = MessagesFragment()
    private var locationHistoryFragments = LocationHistoryFragment()
    private var locationLiveFragment = LocationLiveFragment()
    private var callFragments = CallsFragment()
    private var ussdFragment = USSDFragment()
    private var activeFragment: Fragment = messagesFragment
    private var fragmentManager = supportFragmentManager

    private var firebaseAuth = FirebaseAuth.getInstance()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        initializeFragments(savedInstanceState?.getString("activeFragment"))
        setupToolbar()
        setupDrawer()

    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("activeFragment", activeFragment.tag)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                mDrawer.openDrawer(GravityCompat.START)
                return true
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun setupDrawerContent(drawer: NavigationView) {
        drawer.setNavigationItemSelectedListener {
            selectDrawerItem(it)
            return@setNavigationItemSelectedListener true
        }
    }

    private fun selectDrawerItem(item: MenuItem) {
        when (item.itemId) {
            R.id.messages -> {
                fragmentManager.beginTransaction().hide(activeFragment).show(messagesFragment).commit()
                activeFragment = messagesFragment
            }

            R.id.calls -> {
                fragmentManager.beginTransaction().hide(activeFragment).show(callFragments).commit()
                activeFragment = callFragments
            }

            R.id.ussd -> {
                fragmentManager.beginTransaction().hide(activeFragment).show(ussdFragment).commit()
                activeFragment = ussdFragment
            }

            R.id.location_history -> {
                fragmentManager.beginTransaction().hide(activeFragment).show(locationHistoryFragments).commit()
                activeFragment = locationHistoryFragments
            }

            R.id.location_live -> {
                fragmentManager.beginTransaction().hide(activeFragment).show(locationLiveFragment).commit()
                activeFragment = locationLiveFragment
            }

            R.id.settings -> {
                val settings = Intent(this, SettingsActivity::class.java)
                startActivity(settings)
            }
        }

        if (item.itemId != R.id.settings) {
            title = item.title
            item.isChecked = true
        }

        mDrawer.closeDrawers()

    }

    private fun initializeFragments(active: String?) {
        fragmentManager.beginTransaction().add(R.id.content_main, messagesFragment, "messagesFragment").hide(
            messagesFragment
        ).commit()
        fragmentManager.beginTransaction().add(R.id.content_main, callFragments, "callFragment").hide(callFragments).commit()
        fragmentManager.beginTransaction().add(R.id.content_main, ussdFragment, "ussdFragment").hide(ussdFragment).commit()
        fragmentManager.beginTransaction().add(R.id.content_main, locationHistoryFragments, "locationHistoryFragment").hide(
            locationHistoryFragments
        ).commit()
        fragmentManager.beginTransaction().add(R.id.content_main, locationLiveFragment, "locationLiveFragment").hide(
            locationLiveFragment
        ).commit()

        fragmentManager.executePendingTransactions()

        if (active == null) {
            fragmentManager.beginTransaction().show(messagesFragment).commit()
        } else {
            activeFragment = fragmentManager.findFragmentByTag(active)!!
            fragmentManager.beginTransaction().show(activeFragment).commit()
        }
    }

    private fun setupToolbar() {

        val toolbar = findViewById<androidx.appcompat.widget.Toolbar>(R.id.toolbar)
        toolbar.setNavigationIcon(R.drawable.ic_baseline_menu_24)
        setSupportActionBar(toolbar)

        supportActionBar!!.setDisplayHomeAsUpEnabled(true)
    }

    private fun setupDrawer() {
        mDrawer = findViewById(R.id.drawer_layout)

        navDrawer = findViewById(R.id.navigation)
        setupDrawerContent(navDrawer)

        getDevices()

        setupDrawerHeader()

        logoutButton()
    }

    private fun logoutButton() {
        logout.setOnClickListener {
            val builder = applicationContext.let {
                AlertDialog.Builder(this)
            }
            builder.setMessage(R.string.logout_confirmation)
                    .setTitle(R.string.logout)
                    .setIcon(R.drawable.ic_baseline_power_settings_new_24)
                    .setNegativeButton(R.string.cancel) { dialog, which -> dialog.dismiss() }
                    .setPositiveButton(R.string.logout) { dialog, which ->
                            FirebaseAuth.getInstance().signOut()
                            val loginIntent = Intent(this, LoginActivity::class.java)
                            startActivity(loginIntent)
                            finish()
                    }
                    .show()

        }
    }

    private fun getDevices() {
        val ref = FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().uid}/devices")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val deviceList = ArrayList<DeviceInfoObject>()
                for (data in snapshot.children) {
                    val deviceInfoObject = data.getValue(DeviceInfoObject::class.java)
                    if (deviceInfoObject != null) {
                        deviceList.add(deviceInfoObject)
                    }
                }
                setupSpinner(deviceList)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun setupDrawerHeader() {
        val headerView = navDrawer.getHeaderView(0)

        val emailTextView = headerView.findViewById<TextView>(R.id.user_email)
        emailTextView.text = firebaseAuth.currentUser?.email
    }

    private fun setupSpinner(deviceInfoList: ArrayList<DeviceInfoObject>) {
        var firstLaunch = true //Required to prevent onItemSelected execution during initialization

        val headerView = navDrawer.getHeaderView(0)

        val deviceSpinner = headerView.findViewById<Spinner>(R.id.device_list_spinner)

        val sharedPref = applicationContext.getSharedPreferences(
            getString(R.string.preference_file_key),
            Context.MODE_PRIVATE
        )



        val deviceList = ArrayList<String>()
        deviceInfoList.forEach {
            deviceList.add("${it.model} (${it.device})")
        }



        val adapter = CustomSpinnerAdapter(applicationContext, R.layout.spinner_row, R.id.spinner_text, android.R.layout.simple_spinner_dropdown_item ,deviceList)

        deviceSpinner.adapter = adapter

        deviceSpinner.setSelection(deviceList.indexOf(sharedPref.getString("activeDevice", "")))

        deviceSpinner.onItemSelectedListener = object: AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (!firstLaunch) {
                    val deviceInfo = deviceList[position]
                    val prevSelection = sharedPref.getString("activeDevice", "")
                    sharedPref.edit().putString("activeDevice", deviceInfo).apply()

                    if (prevSelection != deviceInfo) {
                        deleteFiles()
                        restartFragments()
                    }

                    mDrawer.closeDrawers()
                } else {
                    firstLaunch = false
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                mDrawer.closeDrawers()
            }

        }
    }

    private fun deleteFiles() {
        val ussdFile = File(applicationContext.filesDir.toString() + "/USSD.json")
        val callFile = File(applicationContext.filesDir.toString() + "/Calls.json")
        val smsFile = File(applicationContext.filesDir.toString() + "/SMS.json")
        val convFile = File(applicationContext.filesDir.toString() + "/Conversation.json")
        val locationFile = File(applicationContext.filesDir.toString() + "/Location.json")

        val array = arrayListOf(
            ussdFile,
            callFile,
            smsFile,
            convFile,
            locationFile
        )

        array.forEach {
            it.delete()
        }
    }

    private fun restartFragments() {
        for (fragments: Fragment? in supportFragmentManager.fragments) {
            if (fragments != null) {
                supportFragmentManager.beginTransaction().remove(fragments).commit()
            }
        }

        callFragments = CallsFragment()
        messagesFragment = MessagesFragment()
        locationLiveFragment = LocationLiveFragment()
        locationHistoryFragments = LocationHistoryFragment()
        ussdFragment = USSDFragment()

        initializeFragments(activeFragment.tag)
    }

    class CustomSpinnerAdapter(
        context: Context,
        val textViewLayoutId: Int, textViewResourceId: Int,
        val dropDownLayoutId: Int,
        val objects: ArrayList<String>
    ): ArrayAdapter<String>(context!!, textViewLayoutId, textViewResourceId, objects) {
        private val layoutInflater = LayoutInflater.from(context)

        override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = layoutInflater.inflate(dropDownLayoutId, parent, false)
            val textView = view.findViewById<TextView>(android.R.id.text1)
            textView.text = objects[position]
            return view
        }

        override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
            val view = layoutInflater.inflate(textViewLayoutId, parent, false)
            val textView = view.findViewById<TextView>(R.id.spinner_text)
            textView.text = objects[position]
            return view
        }

    }



}