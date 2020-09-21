package com.reas.trackerviewer

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.preference.EditTextPreference
import androidx.preference.PreferenceFragmentCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener

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
        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

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
                    val ref = FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().uid}/settings/${Build.DEVICE}/location")
                    ref.addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            Toast.makeText(context, "Interval updated!", Toast.LENGTH_SHORT).show()
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Log.e(TAG, "onCancelled: Interval failed to update",error.toException() )
                        }

                    })

                    ref.setValue(newValue.toString().toLong())


                    return@setOnPreferenceChangeListener true
                }
            }



        }
    }


}