/*
 * File Name    : BookingSummaryActivity.kt
 * Description  : Booking Summary shown right after a reservation is created, updated
 *                or cancelled. Displays the details returned by the server.
 * Author       : Kandaudahewa C I
 * IT Number    : IT23453142
 * Date         : 2026-09-28
 */
package com.example.solaragrid.ui.reservation

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.solaragrid.R
import com.example.solaragrid.models.ReservationDto
import com.example.solaragrid.ui.dashboard.DashboardActivity
import com.google.gson.Gson

class BookingSummaryActivity : AppCompatActivity() {

    companion object {
        const val ACTION_CREATED = "created"
        const val ACTION_UPDATED = "updated"
        const val ACTION_CANCELLED = "cancelled"
        private const val EXTRA_ACTION = "action"
        private const val EXTRA_RESERVATION = "reservation_json"

        // Builds the intent that opens this summary
        fun intent(context: Context, reservation: ReservationDto, action: String) =
            Intent(context, BookingSummaryActivity::class.java)
                .putExtra(EXTRA_ACTION, action)
                .putExtra(EXTRA_RESERVATION, Gson().toJson(reservation))
    }

    // Fills the summary from the reservation passed in
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_booking_summary)

        val action = intent.getStringExtra(EXTRA_ACTION) ?: ACTION_CREATED
        val r = intent.getStringExtra(EXTRA_RESERVATION)?.let {
            Gson().fromJson(it, ReservationDto::class.java)
        }
        if (r == null) {
            finish()
            return
        }

        val icon = findViewById<TextView>(R.id.tvSummaryIcon)
        val title = findViewById<TextView>(R.id.tvSummaryTitle)
        val text = findViewById<TextView>(R.id.tvSummaryText)
        when (action) {
            ACTION_CANCELLED -> {
                icon.text = "✕"
                icon.setBackgroundResource(R.drawable.bg_circle_cancel)
                icon.setTextColor(ContextCompat.getColor(this, R.color.res_pink))
                title.text = "Reservation cancelled"
                text.text = "The booking is cancelled and its battery slot has been released."
            }
            ACTION_UPDATED -> {
                title.text = "Reservation updated"
                text.text = "Your changes are saved."
            }
            else -> {
                title.text = "Reservation submitted"
                text.text = "Your request is waiting for Backoffice approval. You can follow it under Pending."
            }
        }

        findViewById<TextView>(R.id.tvSumId).text = r.id.takeLast(8).uppercase()
        findViewById<TextView>(R.id.tvSumNode).text = NodeDirectory.nameFor(this, r.nodeId)
        findViewById<TextView>(R.id.tvSumTime).text =
            ReservationTime.dayTime(ReservationTime.parse(r.scheduledDateTime))
        findViewById<TextView>(R.id.tvSumType).text = typeLabel(r.type)
        styleBadge(findViewById(R.id.tvSumStatus), r.status)

        findViewById<Button>(R.id.btnBackDashboard).setOnClickListener { openDashboard("home") }
        findViewById<Button>(R.id.btnViewBookings).setOnClickListener { openDashboard("bookings") }
    }

    // Returns to the existing dashboard on the chosen tab
    private fun openDashboard(tab: String) {
        startActivity(
            Intent(this, DashboardActivity::class.java)
                .putExtra(DashboardActivity.EXTRA_OPEN_TAB, tab)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        )
        finish()
    }
}
