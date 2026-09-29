package com.example.solaragrid.ui.prosumer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.solaragrid.R
import com.example.solaragrid.api.ApiClient
import com.example.solaragrid.api.ProsumerQrApi
import com.example.solaragrid.api.ReservationQrDto
import com.example.solaragrid.database.UserManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast

class ProsumerQrFragment : Fragment() {
    private var historyCall: Call<List<ReservationQrDto>>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_prosumer_qr, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<Button>(R.id.btnRefreshQr).setOnClickListener { loadReservations() }
    }

    override fun onResume() {
        super.onResume()
        loadReservations()
    }

    private fun loadReservations() {
        val root = view ?: return
        val nic = UserManager(requireContext()).getLoggedInUser()?.nic
        val status = root.findViewById<TextView>(R.id.tvQrStatus)
        val list = root.findViewById<LinearLayout>(R.id.qrBookingList)
        val display = root.findViewById<LinearLayout>(R.id.qrDisplay)

        historyCall?.cancel()
        list.removeAllViews()
        display.visibility = View.GONE
        if (nic.isNullOrBlank() || nic.contains('@')) {
            status.text = "Your NIC is unavailable. Please log in with a prosumer account."
            return
        }
        status.text = "Loading reservations..."

        val api = ApiClient.getClient(requireContext().applicationContext)
            .create(ProsumerQrApi::class.java)
        val call = api.getApprovedReservations(nic, "Approved")
        historyCall = call
        call.enqueue(object : Callback<List<ReservationQrDto>> {
            override fun onResponse(
                call: Call<List<ReservationQrDto>>,
                response: Response<List<ReservationQrDto>>
            ) {
                if (view !== root || call !== historyCall || !isAdded) return
                if (!response.isSuccessful) {
                    status.text = "Could not load reservations (HTTP ${response.code()})."
                    return
                }

                val approved = response.body().orEmpty().filter {
                    it.status.equals("Approved", ignoreCase = true) &&
                            !it.qrCode.isNullOrBlank()
                }
                if (approved.isEmpty()) {
                    status.text = "No approved reservations with QR codes yet."
                    return
                }

                status.text = "Choose a reservation:"
                approved.forEach { reservation ->
                    val button = Button(requireContext()).apply {
                        text = "Reservation ${reservation.id.takeLast(8)}\n" +
                                (reservation.scheduledDateTime?.take(16)?.replace('T', ' ') ?: "")
                        setOnClickListener { showQr(root, reservation) }
                    }
                    list.addView(button)
                }
                showQr(root, approved.first())
            }

            override fun onFailure(call: Call<List<ReservationQrDto>>, error: Throwable) {
                if (view !== root || call !== historyCall || call.isCanceled) return
                status.text = "Could not reach the server. Tap Refresh reservations."
            }
        })
    }

    private fun showQr(root: View, reservation: ReservationQrDto) {
        val code = reservation.qrCode ?: return
        val display = root.findViewById<LinearLayout>(R.id.qrDisplay)
        val image = root.findViewById<ImageView>(R.id.ivReservationQr)
        val status = root.findViewById<TextView>(R.id.tvQrStatus)
        try {
            // The encoded value comes from the server. Android only renders it as an image.
            image.setImageBitmap(QrImageRenderer.render(code))
            root.findViewById<TextView>(R.id.tvQrReservation).text =
                "Reservation ${reservation.id.takeLast(8)}"
            root.findViewById<Button>(R.id.btnCopyQrCode).setOnClickListener {
                val clipboard = requireContext()
                    .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Reservation QR", code))
                Toast.makeText(requireContext(), "QR code copied", Toast.LENGTH_SHORT).show()
            }
            display.visibility = View.VISIBLE
        } catch (error: Exception) {
            display.visibility = View.GONE
            status.text = "Could not display this reservation's QR code."
        }
    }

    override fun onDestroyView() {
        historyCall?.cancel()
        historyCall = null
        super.onDestroyView()
    }
}
