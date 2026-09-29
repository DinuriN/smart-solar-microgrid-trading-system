package com.example.solaragrid.ui.prosumer

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.solaragrid.R
import com.example.solaragrid.api.ApiClient
import com.example.solaragrid.api.NodeQrResponse
import com.example.solaragrid.api.ProsumerQrApi
import com.example.solaragrid.api.ReservationQrDto
import com.example.solaragrid.database.UserManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class ProsumerQrFragment : Fragment() {

    private var reservationsCall: Call<List<ReservationQrDto>>? = null
    private val nodeCalls = mutableListOf<Call<NodeQrResponse>>()
    private val nodeNames = mutableMapOf<String, String>()
    private var reservations = emptyList<ReservationQrDto>()
    private var showingHistory = false

    //temp hardcoded reservation for test user
    private var demoPreview = false

    private val sampleReservation = ReservationQrDto(
        id = "6ab2a1d91f596396b20d15ef",
        status = "Approved",
        scheduledDateTime = "2026-09-28T18:00:00.000Z",
        qrCode = "QR-6ab2a1d91f596396b20d15ef-2F8E7FB9A4198F0F75D0E53734A46A246DACECDBD672FE3BB9A30FE818E671E2",
        qrGeneratedAt = "2026-09-26T15:47:50.257Z",
        nodeId = "vavuniya-east",
        batterySlotId = "slot-05"
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_prosumer_qr, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<Button>(R.id.btnRefreshQr).setOnClickListener {
            loadReservations()
        }

        view.findViewById<Button>(R.id.btnCurrentBookings).setOnClickListener {
            showingHistory = false
            renderBookings()
        }

        view.findViewById<Button>(R.id.btnBookingHistory).setOnClickListener {
            showingHistory = true
            renderBookings()
        }

        view.findViewById<EditText>(R.id.etBookingSearch)
            .addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(
                    s: CharSequence?, start: Int, count: Int, after: Int
                ) = Unit

                override fun onTextChanged(
                    s: CharSequence?, start: Int, before: Int, count: Int
                ) {
                    renderBookings()
                }

                override fun afterTextChanged(s: Editable?) = Unit
            })

        view.findViewById<Button>(R.id.btnPreviewSampleBooking).setOnClickListener {
            reservationsCall?.cancel()
            nodeCalls.forEach { it.cancel() }
            nodeCalls.clear()

            demoPreview = true
            showingHistory = false
            reservations = listOf(sampleReservation)
            nodeNames["vavuniya-east"] = "Vavuniya-East"

            view.findViewById<EditText>(R.id.etBookingSearch).setText("")
            renderBookings()
        }

        updateTabs()
    }

    override fun onResume() {
        super.onResume()
        if (demoPreview) {
            renderBookings()
        } else {
            loadReservations()
        }
    }

    private fun loadReservations() {
        val root = view ?: return
        val nic = UserManager(requireContext()).getLoggedInUser()?.nic
        val status = root.findViewById<TextView>(R.id.tvBookingsStatus)
        demoPreview = false

        reservationsCall?.cancel()
        nodeCalls.forEach { it.cancel() }
        nodeCalls.clear()
        reservations = emptyList()
        root.findViewById<LinearLayout>(R.id.bookingList).removeAllViews()

        if (nic.isNullOrBlank() || nic.contains("@")) {
            status.text = "Your prosumer NIC is unavailable. Please log in again."
            return
        }

        status.text = "Loading your bookings…"

        val api = ApiClient.getClient(requireContext().applicationContext)
            .create(ProsumerQrApi::class.java)

        val call = api.getMyReservations(nic)
        reservationsCall = call

        call.enqueue(object : Callback<List<ReservationQrDto>> {
            override fun onResponse(
                call: Call<List<ReservationQrDto>>,
                response: Response<List<ReservationQrDto>>
            ) {
                if (!isAdded || view !== root || call !== reservationsCall) return

                if (!response.isSuccessful) {
                    status.text = "Could not load bookings (HTTP ${response.code()})."
                    return
                }

                reservations = response.body().orEmpty()
                renderBookings()
                loadNodeNames(api, root)
            }

            override fun onFailure(
                call: Call<List<ReservationQrDto>>,
                error: Throwable
            ) {
                if (!isAdded || view !== root || call.isCanceled) return
                status.text = "Could not reach the server. Tap Refresh."
            }
        })
    }

    private fun loadNodeNames(api: ProsumerQrApi, root: View) {
        val objectId = Regex("^[0-9a-fA-F]{24}$")

        reservations.mapNotNull { it.nodeId }
            .filter { objectId.matches(it) && it !in nodeNames }
            .distinct()
            .forEach { nodeId ->
                val call = api.getNode(nodeId)
                nodeCalls.add(call)

                call.enqueue(object : Callback<NodeQrResponse> {
                    override fun onResponse(
                        call: Call<NodeQrResponse>,
                        response: Response<NodeQrResponse>
                    ) {
                        if (!isAdded || view !== root) return
                        val name = response.body()?.data?.nodeName ?: return
                        nodeNames[nodeId] = name
                        renderBookings()
                    }

                    override fun onFailure(
                        call: Call<NodeQrResponse>,
                        error: Throwable
                    ) = Unit
                })
            }
    }

    private fun renderBookings() {
        val root = view ?: return
        val list = root.findViewById<LinearLayout>(R.id.bookingList)
        val status = root.findViewById<TextView>(R.id.tvBookingsStatus)
        val search = root.findViewById<EditText>(R.id.etBookingSearch)
            .text.toString().trim()

        updateTabs()
        list.removeAllViews()

        val displayed = reservations.filter { reservation ->
            val active = reservation.status.equals("Pending", true) ||
                    reservation.status.equals("Approved", true)

            val correctTab = if (showingHistory) !active else active
            val matchesSearch = search.isBlank() ||
                    reservation.id.contains(search, true) ||
                    (reservation.nodeId?.contains(search, true) == true) ||
                    (nodeNames[reservation.nodeId]?.contains(search, true) == true)

            correctTab && matchesSearch
        }

        status.text = when {
            demoPreview -> "Sample preview: this QR has already been verified."
            reservations.isEmpty() -> "No bookings found for your account."
            displayed.isEmpty() -> "No bookings match this tab or search."
            else -> ""
        }

        displayed.forEach { reservation ->
            list.addView(createBookingCard(reservation))
        }
    }

    private fun createBookingCard(reservation: ReservationQrDto): View {
        val context = requireContext()
        val canOpenQr = reservation.status.equals("Approved", true) &&
                !reservation.qrCode.isNullOrBlank()

        val card = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16), dp(15), dp(16), dp(15))
            background = GradientDrawable().apply {
                setColor(Color.parseColor("#131E31"))
                cornerRadius = dp(14).toFloat()
                setStroke(dp(1), Color.parseColor("#26344B"))
            }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                bottomMargin = dp(12)
            }
        }

        val top = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
        }

        val name = TextView(context).apply {
            text = nodeNames[reservation.nodeId]
                ?: reservation.nodeId
                        ?: "Unknown node"
            setTextColor(Color.WHITE)
            textSize = 15f
            setTypeface(null, android.graphics.Typeface.BOLD)
            layoutParams = LinearLayout.LayoutParams(0, -2, 1f)
        }

        val badge = TextView(context).apply {
            text = if (reservation.status.equals("Approved", true) &&
                reservation.qrCode.isNullOrBlank()
            ) {
                "QR pending"
            } else {
                reservation.status
            }
            textSize = 12f
            setPadding(dp(10), dp(5), dp(10), dp(5))
            setTextColor(
                when (reservation.status.lowercase(Locale.ROOT)) {
                    "approved" -> Color.parseColor("#2DD4BF")
                    "pending" -> Color.parseColor("#FBBF24")
                    else -> Color.parseColor("#CBD5E1")
                }
            )
        }

        top.addView(name)
        top.addView(badge)
        card.addView(top)

        card.addView(TextView(context).apply {
            text = "${formatUtc(reservation.scheduledDateTime)}  ·  " +
                    reservation.id.takeLast(8)
            setTextColor(Color.parseColor("#94A3B8"))
            textSize = 12f
            setPadding(0, dp(6), 0, 0)
        })

        if (canOpenQr) {
            card.isClickable = true
            card.isFocusable = true
            card.setOnClickListener {
                parentFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragment_container,
                        TransactionQrFragment.newInstance(reservation)
                    )
                    .addToBackStack(null)
                    .commit()
            }
        }

        return card
    }

    private fun updateTabs() {
        val root = view ?: return
        val current = root.findViewById<Button>(R.id.btnCurrentBookings)
        val history = root.findViewById<Button>(R.id.btnBookingHistory)

        current.backgroundTintList = ColorStateList.valueOf(
            Color.parseColor(if (showingHistory) "#172338" else "#0B1320")
        )
        history.backgroundTintList = ColorStateList.valueOf(
            Color.parseColor(if (showingHistory) "#0B1320" else "#172338")
        )
    }

    private fun formatUtc(value: String?): String {
        if (value.isNullOrBlank()) return "Time unavailable"
        return try {
            val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
                isLenient = false
            }
            val date = input.parse(value.take(19)) ?: return value
            SimpleDateFormat("MMM d · HH:mm", Locale.getDefault()).format(date)
        } catch (_: Exception) {
            value
        }
    }

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    override fun onDestroyView() {
        reservationsCall?.cancel()
        nodeCalls.forEach { it.cancel() }
        nodeCalls.clear()
        super.onDestroyView()
    }
}