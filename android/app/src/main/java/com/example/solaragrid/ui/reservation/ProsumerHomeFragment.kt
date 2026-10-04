/*
 * File Name    : ProsumerHomeFragment.kt
 * Description  : Prosumer Home Dashboard - active / pending counts from the API,
 *                the next upcoming bookings, a slot for Member 2's nearby-nodes card,
 *                and the "+" button that opens the reservation form.
 * Author       : Kandaudahewa C I
 * IT Number    : IT23453142
 * Date         : 2026-09-28
 */
package com.example.solaragrid.ui.reservation

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.solaragrid.R
import com.example.solaragrid.api.ApiClient
import com.example.solaragrid.api.ReservationApi
import com.example.solaragrid.database.UserManager
import com.example.solaragrid.models.ReservationCountsDto
import com.example.solaragrid.models.ReservationDto
import com.example.solaragrid.ui.dashboard.DashboardActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar

class ProsumerHomeFragment : Fragment() {

    private var countsCall: Call<ReservationCountsDto>? = null
    private var historyCall: Call<List<ReservationDto>>? = null
    private lateinit var adapter: BookingAdapter

    // Inflates the dashboard layout
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_prosumer_home, container, false)

    // Sets the greeting, the list and the buttons
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val user = UserManager(requireContext()).getLoggedInUser()
        view.findViewById<TextView>(R.id.tvHomeGreeting).text = greeting()
        view.findViewById<TextView>(R.id.tvHomeName).text = user?.name ?: "Prosumer"
        view.findViewById<TextView>(R.id.tvHomeInitials).text = initials(user?.name)

        adapter = BookingAdapter(
            actionsEnabled = false,
            nodeName = { id -> NodeDirectory.nameFor(requireContext(), id) },
            onModify = {}, onCancel = {}
        )
        view.findViewById<RecyclerView>(R.id.rvUpcoming).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = this@ProsumerHomeFragment.adapter
            isNestedScrollingEnabled = false
        }

        view.findViewById<FloatingActionButton>(R.id.fabNewReservation).setOnClickListener {
            startActivity(ReservationFormActivity.createIntent(requireContext()))
        }
        view.findViewById<TextView>(R.id.tvViewAll).setOnClickListener {
            (activity as? DashboardActivity)?.openBookingsTab()
        }
    }

    // Reloads every time the tab becomes visible, so numbers stay live
    override fun onResume() {
        super.onResume()
        load()
    }

    // Refreshes node names first, then counts and upcoming bookings
    private fun load() {
        val root = view ?: return
        val status = root.findViewById<TextView>(R.id.tvHomeStatus)
        val nic = UserManager(requireContext()).getLoggedInUser()?.nic
        if (nic.isNullOrBlank()) {
            status.text = "Your NIC is unavailable. Please log in with a prosumer account."
            status.visibility = View.VISIBLE
            return
        }
        status.text = "Loading bookings..."
        status.visibility = View.VISIBLE
        NodeDirectory.refresh(requireContext()) {
            if (!isAdded || view == null) return@refresh
            loadCounts(nic)
            loadUpcoming(nic)
        }
    }

    // GET /api/reservations/counts/{nic}
    private fun loadCounts(nic: String) {
        val root = view ?: return
        countsCall?.cancel()
        val call = ApiClient.getClient(requireContext().applicationContext)
            .create(ReservationApi::class.java).getCounts(nic)
        countsCall = call
        call.enqueue(object : Callback<ReservationCountsDto> {
            override fun onResponse(c: Call<ReservationCountsDto>, r: Response<ReservationCountsDto>) {
                if (!isAdded || view !== root) return
                val counts = r.body() ?: return
                root.findViewById<TextView>(R.id.tvActiveCount).text = counts.activeCount.toString()
                root.findViewById<TextView>(R.id.tvPendingCount).text = counts.pendingCount.toString()
            }

            override fun onFailure(c: Call<ReservationCountsDto>, t: Throwable) {}
        })
    }

    // GET /api/reservations/history/{nic} -> next 3 current or pending bookings
    private fun loadUpcoming(nic: String) {
        val root = view ?: return
        val status = root.findViewById<TextView>(R.id.tvHomeStatus)
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
                val upcoming = r.body().orEmpty()
                    .filter { it.tab() != BookingTab.HISTORY }
                    .sortedBy { ReservationTime.parse(it.scheduledDateTime)?.time ?: Long.MAX_VALUE }
                    .take(3)
                adapter.submit(upcoming)
                status.text = "No upcoming bookings. Tap + to reserve a slot."
                status.visibility = if (upcoming.isEmpty()) View.VISIBLE else View.GONE
            }

            override fun onFailure(c: Call<List<ReservationDto>>, t: Throwable) {
                if (!isAdded || view !== root || c.isCanceled) return
                status.text = "Could not reach the server."
            }
        })
    }

    // "Good morning / afternoon / evening"
    private fun greeting(): String = when (Calendar.getInstance().get(Calendar.HOUR_OF_DAY)) {
        in 0..11 -> "Good morning"
        in 12..16 -> "Good afternoon"
        else -> "Good evening"
    }

    // Two-letter avatar text from the name
    private fun initials(name: String?): String =
        name.orEmpty().split(" ", ".").filter { it.isNotBlank() }.take(2)
            .joinToString("") { it.first().uppercase() }.ifBlank { "P" }

    // Cancels running requests when the view goes away
    override fun onDestroyView() {
        countsCall?.cancel()
        historyCall?.cancel()
        super.onDestroyView()
    }
}
