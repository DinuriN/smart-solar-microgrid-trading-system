package com.example.solaragrid.ui.prosumer

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.solaragrid.R
import com.example.solaragrid.api.ApiClient
import com.example.solaragrid.api.NodeQrResponse
import com.example.solaragrid.api.ProsumerQrApi
import com.example.solaragrid.api.ReservationQrDto
import com.example.solaragrid.api.SlotQrResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class TransactionQrFragment : Fragment() {

    private var nodeCall: Call<NodeQrResponse>? = null
    private var slotCall: Call<SlotQrResponse>? = null

    companion object {
        fun newInstance(reservation: ReservationQrDto): TransactionQrFragment {
            return TransactionQrFragment().apply {
                arguments = Bundle().apply {
                    putString("id", reservation.id)
                    putString("qrCode", reservation.qrCode)
                    putString("nodeId", reservation.nodeId)
                    putString("slotId", reservation.batterySlotId)
                    putString("scheduled", reservation.scheduledDateTime)
                    putString("generated", reservation.qrGeneratedAt)
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = inflater.inflate(R.layout.fragment_transaction_qr, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val args = requireArguments()
        val id = args.getString("id").orEmpty()
        val code = args.getString("qrCode").orEmpty()
        val nodeId = args.getString("nodeId").orEmpty()
        val slotId = args.getString("slotId").orEmpty()

        view.findViewById<TextView>(R.id.tvTransactionReservation).text = id
        view.findViewById<TextView>(R.id.tvTransactionNode).text =
            nodeId.ifBlank { "Unavailable" }
        view.findViewById<TextView>(R.id.tvTransactionSlot).text =
            formatUtc(args.getString("scheduled"), "MMM d, HH:mm")
        view.findViewById<TextView>(R.id.tvTransactionGenerated).text =
            formatUtc(args.getString("generated"), "HH:mm")

        try {
            view.findViewById<ImageView>(R.id.ivTransactionQr)
                .setImageBitmap(QrImageRenderer.render(code))
        } catch (_: Exception) {
            view.findViewById<TextView>(R.id.tvTransactionHint).text =
                "Could not display this QR code."
        }

        view.findViewById<Button>(R.id.btnTransactionBack).setOnClickListener {
            parentFragmentManager.popBackStack()
        }

        view.findViewById<Button>(R.id.btnTransactionCopy).setOnClickListener {
            val clipboard = requireContext()
                .getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            clipboard.setPrimaryClip(ClipData.newPlainText("Reservation QR", code))
            Toast.makeText(requireContext(), "QR code copied", Toast.LENGTH_SHORT).show()
        }

        // These existing read endpoints supply the node name and actual slot interval.
        // If a test reservation has placeholder IDs, the basic details remain visible.
        val validId = Regex("^[0-9a-fA-F]{24}$")
        if (!validId.matches(nodeId)) return

        val api = ApiClient.getClient(requireContext().applicationContext)
            .create(ProsumerQrApi::class.java)

        nodeCall = api.getNode(nodeId).also { call ->
            call.enqueue(object : Callback<NodeQrResponse> {
                override fun onResponse(
                    call: Call<NodeQrResponse>,
                    response: Response<NodeQrResponse>
                ) {
                    if (!isAdded || this@TransactionQrFragment.view !== view) return
                    response.body()?.data?.nodeName?.let { name ->
                        view.findViewById<TextView>(R.id.tvTransactionNode).text = name
                    }
                }

                override fun onFailure(call: Call<NodeQrResponse>, error: Throwable) = Unit
            })
        }

        if (!validId.matches(slotId)) return

        slotCall = api.getSlot(nodeId, slotId).also { call ->
            call.enqueue(object : Callback<SlotQrResponse> {
                override fun onResponse(
                    call: Call<SlotQrResponse>,
                    response: Response<SlotQrResponse>
                ) {
                    if (!isAdded || this@TransactionQrFragment.view !== view) return
                    val slot = response.body()?.data ?: return
                    if (slot.startTime != null && slot.endTime != null) {
                        view.findViewById<TextView>(R.id.tvTransactionSlot).text =
                            "${formatUtc(slot.startTime, "MMM d, HH:mm")}–" +
                                    formatUtc(slot.endTime, "HH:mm")
                    }
                }

                override fun onFailure(call: Call<SlotQrResponse>, error: Throwable) = Unit
            })
        }
    }

    private fun formatUtc(value: String?, outputPattern: String): String {
        if (value.isNullOrBlank()) return "Unavailable"
        return try {
            val input = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
                timeZone = TimeZone.getTimeZone("UTC")
                isLenient = false
            }
            val date = input.parse(value.take(19)) ?: return value
            SimpleDateFormat(outputPattern, Locale.getDefault()).format(date)
        } catch (_: Exception) {
            value
        }
    }

    override fun onDestroyView() {
        nodeCall?.cancel()
        slotCall?.cancel()
        super.onDestroyView()
    }
}