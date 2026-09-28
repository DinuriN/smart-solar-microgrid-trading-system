/*
 * File Name    : MyBookingsFragment.kt
 * Description  : My Bookings - Current / Pending / History tabs, a search box, and
 *                Modify / Cancel actions on Current and Pending bookings.
 * Author       : Kandaudahewa C I
 * IT Number    : IT23453142
 * Date         : 2026-09-28
 */
package com.example.solaragrid.ui.reservation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.solaragrid.R
import com.example.solaragrid.api.ApiClient
import com.example.solaragrid.api.ReservationApi
import com.example.solaragrid.database.UserManager
import com.example.solaragrid.models.ReservationDto
import com.google.android.material.tabs.TabLayout
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MyBookingsFragment : Fragment() {

    private var all: List<ReservationDto> = emptyList()
    private var currentTab = BookingTab.CURRENT
    private var historyCall: Call<List<ReservationDto>>? = null
    private lateinit var adapter: BookingAdapter

    // Inflates the My Bookings layout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_my_bookings, container, false)

    // Wires the list, tabs and search box
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = BookingAdapter(
            actionsEnabled = true,
            nodeName = { id -> NodeDirectory.nameFor(requireContext(), id) },
            onModify = { r -> startActivity(ReservationFormActivity.modifyIntent(requireContext(), r)) },
            onCancel = { r -> confirmCancel(r) }
        )
        view.findViewById<RecyclerView>(R.id.rvBookings).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@MyBookingsFragment.adapter
        }

        view.findViewById<TabLayout>(R.id.tabBookings)
            .addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
                override fun onTabSelected(tab: TabLayout.Tab) {
                    currentTab = BookingTab.values()[tab.position]
                    render()
                }
                override fun onTabUnselected(tab: TabLayout.Tab) {}
                override fun onTabReselected(tab: TabLayout.Tab) {}
            })

        view.findViewById<EditText>(R.id.etSearchBookings).doAfterTextChanged { render() }
    }

    // Reloads whenever the tab is shown again (e.g. after modify / cancel)
    override fun onResume() {
        super.onResume()
        loadBookings()
    }

    // GET /api/reservations/history/{nic}
    private fun loadBookings() {
        val root = view ?: return
        val status = root.findViewById<TextView>(R.id.tvBookingsStatus)
        val nic = UserManager(requireContext()).getLoggedInUser()?.nic
        if (nic.isNullOrBlank()) {
            status.text = "Your NIC is unavailable. Please log in with a prosumer account."
            status.visibility = View.VISIBLE
            return
        }
        status.text = "Loading bookings..."
        status.visibility = View.VISIBLE

        NodeDirectory.refresh(requireContext()) {
            if (!isAdded || view !== root) return@refresh
            historyCall?.cancel()
            val call = ApiClient.getClient(requireContext().applicationContext)
                .create(ReservationApi::class.java).getHistory(nic)
            historyCall = call
            call.enqueue(object : Callback<List<ReservationDto>> {
                override fun onResponse(c: Call<List<ReservationDto>>, r: Response<List<ReservationDto>>) {
                    if (!isAdded || view !== root) return
                    if (!r.isSuccessful) {
                        status.text = "Could not load bookings (HTTP ${r.code()})."
                        return
                    }
                    all = r.body().orEmpty()
                    render()
                }

                override fun onFailure(c: Call<List<ReservationDto>>, t: Throwable) {
                    if (!isAdded || view !== root || c.isCanceled) return
                    status.text = "Could not reach the server."
                }
            })
        }
    }

    // Shows the bookings of the selected tab that match the search text
    private fun render() {
        val root = view ?: return
        val query = root.findViewById<EditText>(R.id.etSearchBookings).text.toString().trim().lowercase()
        val list = all
            .filter { it.tab() == currentTab }
            .filter {
                query.isEmpty() ||
                    it.id.lowercase().contains(query) ||
                    NodeDirectory.nameFor(requireContext(), it.nodeId).lowercase().contains(query)
            }
            .sortedBy { ReservationTime.parse(it.scheduledDateTime)?.time ?: 0L }
            .let { if (currentTab == BookingTab.HISTORY) it.reversed() else it }

        adapter.submit(list)
        val status = root.findViewById<TextView>(R.id.tvBookingsStatus)
        status.text = if (query.isEmpty()) "No bookings here." else "No bookings match \"$query\"."
        status.visibility = if (list.isEmpty()) View.VISIBLE else View.GONE
    }

    // Confirmation dialog before cancelling
    private fun confirmCancel(r: ReservationDto) {
        val name = NodeDirectory.nameFor(requireContext(), r.nodeId)
        val time = ReservationTime.dayTime(ReservationTime.parse(r.scheduledDateTime))
        AlertDialog.Builder(requireContext())
            .setTitle("Cancel reservation?")
            .setMessage("$name · $time\nThe battery slot will be released. This can't be undone.")
            .setNegativeButton("Keep", null)
            .setPositiveButton("Cancel reservation") { _, _ -> cancel(r) }
            .show()
    }

    // DELETE /api/reservations/{id} -> Booking Summary
    private fun cancel(r: ReservationDto) {
        val ctx = requireContext()
        ApiClient.getClient(ctx.applicationContext).create(ReservationApi::class.java)
            .cancel(r.id).enqueue(object : Callback<Void> {
                override fun onResponse(c: Call<Void>, response: Response<Void>) {
                    if (!isAdded) return
                    if (response.isSuccessful) {
                        // DELETE has no body, so the summary uses this booking's own details
                        startActivity(
                            BookingSummaryActivity.intent(
                                ctx, r.copy(status = "Cancelled"), BookingSummaryActivity.ACTION_CANCELLED
                            )
                        )
                    } else {
                        Toast.makeText(ctx, apiErrorMessage(response, "Could not cancel this reservation."), Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(c: Call<Void>, t: Throwable) {
                    if (!isAdded) return
                    Toast.makeText(ctx, t.message ?: "Could not reach the server.", Toast.LENGTH_LONG).show()
                }
            })
    }

    // Cancels the running request when the view goes away
    override fun onDestroyView() {
        historyCall?.cancel()
        super.onDestroyView()
    }
}
