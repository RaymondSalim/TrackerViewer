package com.reas.trackerviewer

import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.fragment.app.Fragment
import com.google.android.material.navigation.NavigationView
import com.google.firebase.auth.FirebaseAuth
import com.reas.trackerviewer.calls.CallsFragment
import com.reas.trackerviewer.messages.MessagesFragment
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {
    private lateinit var mDrawer:DrawerLayout
    private lateinit var navDrawer: NavigationView

    private var messagesFragment = MessagesFragment()
    private var locationFragments = LocationFragments()
    private var callFragments = CallsFragment()
    private var ussdFragment = USSDFragment()
    private var activeFragment: Fragment = messagesFragment
    private var fragmentManager = supportFragmentManager

    private var firebaseAuth = FirebaseAuth.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initializeFragments()
        setupToolbar()
        setupDrawer()

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

            R.id.location -> {
                fragmentManager.beginTransaction().hide(activeFragment).show(locationFragments).commit()
                activeFragment = locationFragments
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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
    when (item.itemId) {
        android.R.id.home -> {
            mDrawer.openDrawer(GravityCompat.START)
            return true
        }
    }

        return super.onOptionsItemSelected(item)
    }

    private fun initializeFragments() {
        fragmentManager.beginTransaction().add(R.id.content_main, messagesFragment, "messagesFragment").commit()
        fragmentManager.beginTransaction().add(R.id.content_main, callFragments, "callFragment").hide(callFragments).commit()
        fragmentManager.beginTransaction().add(R.id.content_main, ussdFragment, "ussdFragment").hide(ussdFragment).commit()
        fragmentManager.beginTransaction().add(R.id.content_main, locationFragments, "locationFragment").hide(locationFragments).commit()
        fragmentManager.executePendingTransactions()

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

        navDrawer.getHeaderView(0).findViewById<TextView>(R.id.user_email).text = firebaseAuth.currentUser?.email

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
                    .setPositiveButton(R.string.logout) {
                        dialog, which ->
                            FirebaseAuth.getInstance().signOut()
                            val loginIntent = Intent(this, LoginActivity::class.java)
                            startActivity(loginIntent)
                            finish()
                    }
                    .show()

            val dialog = builder.create()

        }
    }
}