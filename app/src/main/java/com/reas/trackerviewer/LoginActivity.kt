package com.reas.trackerviewer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import kotlinx.android.synthetic.main.activity_login.*

class LoginActivity : AppCompatActivity() {
    private lateinit var auth: FirebaseAuth

    private val TAG = "FirebaseAuth"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        auth = FirebaseAuth.getInstance()

        loginButton.setOnClickListener {
            it.isEnabled = false
            signIn(textEmail.text.toString(), textPassword.text.toString())
        }

        registerButton.setOnClickListener {
            it.isEnabled = false
            createUser(textEmail.text.toString(), textPassword.text.toString())
        }
    }

    override fun onStart() {
        super.onStart()
        if (auth.currentUser != null) {
            startMainActivity()
            finish()
        }
    }

    private fun createUser(email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this) {
            if (it.isSuccessful) {
                Toast.makeText(applicationContext, getString(R.string.user_create_success), Toast.LENGTH_SHORT).show()

            } else {
                Log.e(TAG, "createUser: ${it.exception.toString()}", )
                Toast.makeText(applicationContext, getString(R.string.user_create_fail), Toast.LENGTH_SHORT).show()

            }
            registerButton.isEnabled = true
        }
    }

    private fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this) {
            if (it.isSuccessful) {
                Toast.makeText(this, getString(R.string.login_success), Toast.LENGTH_SHORT).show()

                getDevices()

            } else {
                Log.e(TAG, "signIn: ${it.exception.toString()}")
                Toast.makeText(this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
                loginButton.isEnabled = true
            }
        }
    }

    private fun getDevices() {
        val ref = FirebaseDatabase.getInstance().getReference("users/${FirebaseAuth.getInstance().uid}/devices")
        ref.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                for (data in snapshot.children) {
                    val deviceList = ArrayList<DeviceInfoObject>()
                    val deviceInfoObject = data.getValue(DeviceInfoObject::class.java)
                    if (deviceInfoObject != null) {
                        deviceList.add(deviceInfoObject)
                    }

                    val device = deviceList[0]

                    getSharedPreferences(getString(R.string.preference_file_key), Context.MODE_PRIVATE).edit().putString("activeDevice", "${device.model} (${device.device})").commit()
                    startMainActivity()
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)

        finish()
    }
}