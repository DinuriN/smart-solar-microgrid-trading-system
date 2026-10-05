package com.example.solaragrid.ui.onboarding

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.solaragrid.MainActivity
import com.example.solaragrid.R

class OnboardingActivity : AppCompatActivity() {

    private val images = arrayOf(
        R.drawable.ic_onboarding_welcome,
        R.drawable.ic_onboarding_nodes,
        R.drawable.ic_onboarding_transfer
    )

    private val titles = arrayOf(
        "Welcome to Solara Grid",
        "Manage Microgrid Nodes",
        "Book & Transfer Energy"
    )

    private val descriptions = arrayOf(
        "Your intelligent assistant for managing solar energy distribution efficiently.",
        "Monitor your local energy nodes, check capacities, and manage resources.",
        "Easily reserve energy slots and verify transactions at grid stations securely."
    )

    private var currentIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_onboarding)

        val imageView = findViewById<ImageView>(R.id.ivOnboardingImage)
        val titleText = findViewById<TextView>(R.id.tvOnboardingTitle)
        val descriptionText = findViewById<TextView>(R.id.tvOnboardingDescription)
        val btnNext = findViewById<Button>(R.id.btnNext)
        val btnSkip = findViewById<Button>(R.id.btnSkip)

        fun updateUI() {
            imageView.setImageResource(images[currentIndex])
            titleText.text = titles[currentIndex]
            descriptionText.text = descriptions[currentIndex]
            if (currentIndex == titles.size - 1) {
                btnNext.text = "Get Started"
            } else {
                btnNext.text = "Next"
            }
        }

        updateUI()

        btnNext.setOnClickListener {
            if (currentIndex < titles.size - 1) {
                currentIndex++
                updateUI()
            } else {
                finishOnboarding()
            }
        }

        btnSkip.setOnClickListener {
            finishOnboarding()
        }
    }

    private fun finishOnboarding() {
        val sharedPrefs = getSharedPreferences("SolaraGridPrefs", Context.MODE_PRIVATE)
        sharedPrefs.edit().putBoolean("hasSeenOnboarding", true).apply()
        
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }
}
