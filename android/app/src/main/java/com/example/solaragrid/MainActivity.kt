package com.example.solaragrid

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // TODO: Later we will check SQLite UserManager to see if they are logged in.
        // For now, immediately route to the Login Screen.
        
        val intent = android.content.Intent(this, com.example.solaragrid.ui.auth.LoginActivity::class.java)
        startActivity(intent)
        finish() // Close MainActivity so the user can't press 'Back' to return to a blank screen
    }
}