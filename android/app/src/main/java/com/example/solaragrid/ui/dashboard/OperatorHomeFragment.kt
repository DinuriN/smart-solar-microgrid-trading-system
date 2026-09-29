package com.example.solaragrid.ui.dashboard

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.solaragrid.R
import com.example.solaragrid.database.UserManager
import android.widget.Button
import com.example.solaragrid.api.ApiClient
import com.example.solaragrid.api.DashboardStatsApi
import com.example.solaragrid.api.DashboardStatsResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class OperatorHomeFragment : Fragment() {
    private var statsCall: Call<DashboardStatsResponse>? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_operator_home, container, false)

        val tvOpName = view.findViewById<TextView>(R.id.tvOpName)
        val btnAvatar = view.findViewById<TextView>(R.id.btnAvatar)

        //Get the Operator's name from SQLite
        val user = UserManager(requireContext()).getLoggedInUser()
        val fullName = user?.name ?: "Operator"
        
        tvOpName.text = fullName

        //Generate Initials
        val initials = fullName.split(" ").mapNotNull { it.firstOrNull()?.toString() }.joinToString("").take(2).uppercase()
        btnAvatar.text = initials

        //Click listener for the Avatar to Log Out
        btnAvatar.setOnClickListener {
            AlertDialog.Builder(requireContext())
                .setTitle("Log Out")
                .setMessage("Are you sure you want to log out?")
                .setPositiveButton("Yes") { _, _ ->
                    // Clear SQLite and route to Login
                    UserManager(requireContext()).clearUser()
                    val intent = android.content.Intent(requireContext(), com.example.solaragrid.ui.auth.LoginActivity::class.java)
                    intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }

        view.findViewById<Button>(R.id.btnRefreshStats).setOnClickListener {
            fetchStats()
        }

        return view
    }

    override fun onResume() {
        super.onResume()
        fetchStats()
    }

    private fun fetchStats() {
        val root = view ?: return
        root.findViewById<TextView>(R.id.tvStatsMessage).text =
            "Loading dashboard..."
        root.findViewById<TextView>(R.id.tvPendingReservations).text = "—"
        root.findViewById<TextView>(R.id.tvApprovedFutureReservations).text = "—"

        statsCall?.cancel()
        val api = ApiClient.getClient(requireContext().applicationContext)
            .create(DashboardStatsApi::class.java)
        val call = api.getStats()
        statsCall = call

        call.enqueue(object : Callback<DashboardStatsResponse> {
            override fun onResponse(
                call: Call<DashboardStatsResponse>,
                response: Response<DashboardStatsResponse>
            ) {
                val currentView = view ?: return
                if (!isAdded || call !== statsCall) return

                val stats = response.body()
                if (response.isSuccessful && stats != null) {
                    currentView.findViewById<TextView>(
                        R.id.tvPendingReservations
                    ).text = stats.pendingReservations.toString()

                    currentView.findViewById<TextView>(
                        R.id.tvApprovedFutureReservations
                    ).text = stats.approvedFutureReservations.toString()

                    currentView.findViewById<TextView>(R.id.tvStatsMessage).text = ""
                } else {
                    currentView.findViewById<TextView>(R.id.tvStatsMessage).text =
                        "Could not load stats (HTTP ${response.code()}). Tap Refresh stats."
                }
            }

            override fun onFailure(
                call: Call<DashboardStatsResponse>,
                error: Throwable
            ) {
                val currentView = view ?: return
                if (!isAdded || call !== statsCall || call.isCanceled) return

                currentView.findViewById<TextView>(R.id.tvStatsMessage).text =
                    "Could not reach the server. Tap Refresh stats."
            }
        })
    }

    override fun onDestroyView() {
        statsCall?.cancel()
        statsCall = null
        super.onDestroyView()
    }
}
