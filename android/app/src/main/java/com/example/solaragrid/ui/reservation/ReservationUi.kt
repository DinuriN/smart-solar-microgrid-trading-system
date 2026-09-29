/*
 * File Name    : ReservationUi.kt
 * Description  : Shared display helpers for the reservation screens: date parsing and
 *                formatting, tab grouping, status badges, node names and API error text.
 * Author       : Kandaudahewa C I
 * IT Number    : IT23453142
 * Date         : 2026-09-28
 */
package com.example.solaragrid.ui.reservation

import android.content.Context
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.example.solaragrid.R
import com.example.solaragrid.api.ApiClient
import com.example.solaragrid.api.NodeApi
import com.example.solaragrid.database.CachedNode
import com.example.solaragrid.database.NodeCacheHelper
import com.example.solaragrid.models.ApiResponse
import com.example.solaragrid.models.NodeDto
import com.example.solaragrid.models.ReservationDto
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

// ---------- Dates ----------
object ReservationTime {

    // Builds a formatter that reads/writes UTC
    private fun utc(pattern: String) =
        SimpleDateFormat(pattern, Locale.US).apply { timeZone = TimeZone.getTimeZone("UTC") }

    // Parses the API's ISO-8601 strings ("...Z", "...+05:30" or no zone = UTC)
    fun parse(iso: String?): Date? {
        if (iso.isNullOrBlank() || iso.length < 19) return null
        return try {
            val base = utc("yyyy-MM-dd'T'HH:mm:ss").parse(iso.substring(0, 19)) ?: return null
            val offset = Regex("([+-])(\\d{2}):(\\d{2})$").find(iso.substring(19))
            if (offset == null) base else {
                val sign = if (offset.groupValues[1] == "+") 1 else -1
                val minutes = offset.groupValues[2].toInt() * 60 + offset.groupValues[3].toInt()
                Date(base.time - sign * minutes * 60_000L)
            }
        } catch (e: Exception) {
            null
        }
    }

    // Formats a Date for the API (UTC, ISO-8601)
    fun toIsoUtc(date: Date): String = utc("yyyy-MM-dd'T'HH:mm:ss'Z'").format(date)

    // "Sep 30 · 14:00" in the phone's local time
    fun dayTime(date: Date?): String =
        if (date == null) "-" else SimpleDateFormat("MMM d · HH:mm", Locale.US).format(date)

    // "14:00" in the phone's local time
    fun time(date: Date?): String =
        if (date == null) "--:--" else SimpleDateFormat("HH:mm", Locale.US).format(date)
}

// ---------- Tabs and display state ----------
enum class BookingTab { CURRENT, PENDING, HISTORY }

// Which My Bookings tab a reservation belongs to (display grouping of server statuses)
fun ReservationDto.tab(now: Date = Date()): BookingTab {
    val start = ReservationTime.parse(scheduledDateTime)
    return when {
        status.equals("Pending", true) -> BookingTab.PENDING
        status.equals("Approved", true) && start != null && start.after(now) -> BookingTab.CURRENT
        else -> BookingTab.HISTORY
    }
}

// Greys out Modify/Cancel when the booking is under 12 hours away (server re-checks)
fun ReservationDto.isLocked(now: Date = Date()): Boolean {
    val start = ReservationTime.parse(scheduledDateTime) ?: return true
    return start.time - now.time < 12 * 60 * 60 * 1000L
}

// Human label for the reservation type
fun typeLabel(type: String): String =
    if (type.equals("EnergyDropOff", true)) "Energy drop-off" else "Charging"

// Colours a status pill to match the prototype
fun styleBadge(badge: TextView, status: String) {
    val (background, textColor) = when (status.lowercase()) {
        "approved" -> R.drawable.bg_badge_approved to R.color.res_teal
        "pending" -> R.drawable.bg_badge_pending to R.color.solara_yellow
        "blocked" -> R.drawable.bg_badge_blocked to R.color.res_pink
        else -> R.drawable.bg_badge_neutral to R.color.res_gray
    }
    badge.text = status.lowercase()
    badge.setBackgroundResource(background)
    badge.setTextColor(ContextCompat.getColor(badge.context, textColor))
}

// Reads the { "message": "..." } body the API returns with 400 errors
fun apiErrorMessage(response: Response<*>, fallback: String): String {
    return try {
        val body = response.errorBody()?.string()
        if (body.isNullOrBlank()) fallback
        else JSONObject(body).optString("message", fallback).ifBlank { fallback }
    } catch (e: Exception) {
        fallback
    }
}

// ---------- Node names (reference data cached in SQLite) ----------
object NodeDirectory {

    // Downloads the node list and caches it; always calls done(), even offline
    fun refresh(context: Context, done: () -> Unit) {
        val app = context.applicationContext
        ApiClient.getClient(app).create(NodeApi::class.java).getNodes()
            .enqueue(object : Callback<ApiResponse<List<NodeDto>>> {
                override fun onResponse(
                    call: Call<ApiResponse<List<NodeDto>>>,
                    response: Response<ApiResponse<List<NodeDto>>>
                ) {
                    val nodes = response.body()?.data
                    if (response.isSuccessful && nodes != null) NodeCacheHelper(app).replaceAll(nodes)
                    done()
                }

                override fun onFailure(call: Call<ApiResponse<List<NodeDto>>>, t: Throwable) {
                    done() // fall back to the cached names
                }
            })
    }

    // All cached nodes for the node picker
    fun all(context: Context): List<CachedNode> = NodeCacheHelper(context).getAll()

    // Name of a node, or a short id when it is not cached
    fun nameFor(context: Context, id: String): String =
        NodeCacheHelper(context).nameFor(id) ?: "Node ${id.takeLast(6)}"
}
