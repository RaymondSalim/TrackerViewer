package com.reas.trackerviewer

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

private const val TAG = "SettingsActivity"

class SettingsActivity : AppCompatActivity() {



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        supportFragmentManager
            .beginTransaction()
            .replace(R.id.settings, SettingsFragment())
            .commit()

        val toolbar = findViewById<Toolbar>(R.id.settings_toolbar)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    class SettingsFragment : PreferenceFragmentCompat() {

        var deviceID:String? = null

        var ref: DatabaseReference? = null

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)

            val settings = PreferenceManager.getDefaultSharedPreferences(context)

            ref = FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().uid}/settings/${deviceID}/location")

            ref?.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
//                    val pm = getDefaultSharedPreferences(context)
//                    pm.edit().putString("locationInterval", snapshot.value.toString())

//                    preferenceManager.preferenceDataStore?.putString("locationInterval", snapshot.value.toString())
//                    preferenceScreen.sharedPreferences.edit().putString("locationInterval", snapshot.value.toString())
//                    preferenceManager.sharedPreferences.edit().putString("locationInterval", snapshot.value.toString())

                    settings.edit().putString("locationInterval", snapshot.value.toString()).apply()

                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e(TAG, "onCancelled: Interval failed to update", error.toException())
                }

            })

        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            deviceID = context?.getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE)?.getString("activeDevice", "")
            deviceID = deviceID?.substring(deviceID?.indexOf("(")?.plus(1)!!, deviceID?.indexOf(")")!!)
            
            
            initializePreferences()
        }

        private fun initializePreferences() {
            val locationIntervalPref = preferenceScreen.findPreference<EditTextPreference>("locationInterval")

            locationIntervalPref?.setOnPreferenceChangeListener { preference, newValue ->
                if ((newValue.toString()).toLong() < 5000) {
                    Toast.makeText(requireContext(), "Value should be greater than 5000ms", Toast.LENGTH_SHORT).show()
                    return@setOnPreferenceChangeListener false
                }
                else {
                    ref?.setValue(newValue.toString().toLong())


                    return@setOnPreferenceChangeListener true
                }
            }



        }
    }


}