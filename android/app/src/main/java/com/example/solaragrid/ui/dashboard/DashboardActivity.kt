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

    // [Member 3] Lets other screens (e.g. Booking Summary) ask for a specific tab
    companion object {
        const val EXTRA_OPEN_TAB = "open_tab"
    }

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
            openRequestedTab(intent)
        }
    }

    // [Member 3] Called when the Booking Summary brings this screen back to the front
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        openRequestedTab(intent)
    }

    // [Member 3] Used by the Home "View all" link
    fun openBookingsTab() {
        findViewById<BottomNavigationView>(R.id.bottom_navigation).selectedItemId = R.id.nav_bookings
    }

    // [Member 3] Switches to the tab named in the intent ("home" or "bookings")
    private fun openRequestedTab(intent: Intent?) {
        val tab = intent?.getStringExtra(EXTRA_OPEN_TAB) ?: return
        val nav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val target = if (tab == "bookings") R.id.nav_bookings else R.id.nav_home
        if (nav.selectedItemId == target) {
            loadFragment(ComingSoonFragment.newInstance(if (tab == "bookings") "Bookings" else "Home"))
        } else {
            nav.selectedItemId = target
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}
