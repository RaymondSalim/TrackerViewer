package com.reas.trackerviewer

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
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
                startMainActivity()
                finish()
            } else {
                Log.e(TAG, "signIn: ${it.exception.toString()}")
                Toast.makeText(this, getString(R.string.login_failed), Toast.LENGTH_SHORT).show()
                loginButton.isEnabled = true
            }
        }
    }

    private fun startMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
    }
}