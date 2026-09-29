package com.example.solaragrid

import android.os.Bundle
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import com.example.solaragrid.database.UserManager
import com.example.solaragrid.ui.auth.LoginActivity
import com.example.solaragrid.ui.dashboard.DashboardActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check SQLite UserManager to see if they are already logged in
        val user = UserManager(this).getLoggedInUser()
        
        if (user != null) {
            // User exists in SQLite, skip login and go straight to Dashboard!
            startActivity(Intent(this, DashboardActivity::class.java))
        } else {
            // No user found, go to Login Screen
            startActivity(Intent(this, LoginActivity::class.java))
        }
        
        // Close MainActivity so the user can't press 'Back' to return to a blank screen
        finish()
    }
}