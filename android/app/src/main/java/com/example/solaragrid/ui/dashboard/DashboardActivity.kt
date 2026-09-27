package com.example.solaragrid.ui.dashboard

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.solaragrid.R
import com.example.solaragrid.database.UserManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.content.Intent
import com.example.solaragrid.ui.operator.GridOperatorScanActivity
import com.example.solaragrid.ui.prosumer.ProsumerQrFragment

class DashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_dashboard)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        
        // Get user role from SQLite
        val user = UserManager(this).getLoggedInUser()
        val role = user?.role ?: ""

        if (role.equals("GridOperator", ignoreCase = true)) {
            //OPERATOR MODE
            bottomNav.inflateMenu(R.menu.bottom_nav_operator)
            loadFragment(OperatorHomeFragment())

            // REFERENCE: The logic to listen to BottomNavigationView clicks and swap 
            // the main FrameLayout with different Fragments was adapted from GeeksforGeeks.
            // Source: https://www.geeksforgeeks.org/bottom-navigation-bar-in-android/
            bottomNav.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_op_home -> {
                        if (supportFragmentManager.findFragmentById(R.id.fragment_container)
                                    !is OperatorHomeFragment
                        ) {
                            loadFragment(OperatorHomeFragment())
                        }
                        true
                    }

                    R.id.nav_op_scan -> {
                        startActivity(Intent(this, GridOperatorScanActivity::class.java))
                        false // Keep Home selected when returning from the scanner
                    }

                    else -> false
                }
            }
        } else {
            //PROSUMER MODE
            bottomNav.inflateMenu(R.menu.bottom_nav_prosumer)
            loadFragment(ComingSoonFragment.newInstance("Home"))

            // REFERENCE: Fragment swapping listener adapted from standard Android UI tutorials.
            bottomNav.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> loadFragment(ComingSoonFragment.newInstance("Home"))
                    R.id.nav_map -> loadFragment(ComingSoonFragment.newInstance("Map"))
                    R.id.nav_bookings -> loadFragment(ComingSoonFragment.newInstance("Bookings"))
                    R.id.nav_profile -> loadFragment(ProsumerProfileFragment())
                    R.id.nav_qr -> loadFragment(ProsumerQrFragment())
                }
                true
            }
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
