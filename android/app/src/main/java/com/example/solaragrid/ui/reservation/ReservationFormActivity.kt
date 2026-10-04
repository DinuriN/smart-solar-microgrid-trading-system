/*
 * File Name    : ReservationFormActivity.kt
 * Description  : One form for both Create and Modify reservation. Create: pick node,
 *                arrival time, free battery slot and type. Modify: same layout, fields
 *                pre-filled, node locked.
 * Author       : Kandaudahewa C I
 * IT Number    : IT23453142
 * Date         : 2026-09-28
 */
package com.example.solaragrid.ui.reservation

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.RadioButton
import android.widget.Spinner
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.solaragrid.R
import com.example.solaragrid.api.ApiClient
import com.example.solaragrid.api.NodeApi
import com.example.solaragrid.api.ReservationApi
import com.example.solaragrid.database.CachedNode
import com.example.solaragrid.models.ApiResponse
import com.example.solaragrid.models.BatterySlotDto
import com.example.solaragrid.models.CreateReservationRequest
import com.example.solaragrid.models.ReservationDto
import com.example.solaragrid.models.UpdateReservationRequest
import com.google.gson.Gson
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.util.Date

class ReservationFormActivity : AppCompatActivity() {

    companion object {
        private const val EXTRA_RESERVATION = "reservation_json"

        // Opens the form empty (Create)
        fun createIntent(context: Context) = Intent(context, ReservationFormActivity::class.java)

        // Opens the form pre-filled (Modify)
        fun modifyIntent(context: Context, reservation: ReservationDto) =
            Intent(context, ReservationFormActivity::class.java)
                .putExtra(EXTRA_RESERVATION, Gson().toJson(reservation))
    }

    // One option in the battery slot dropdown
    private data class SlotOption(val id: String, val label: String)

    private var editing: ReservationDto? = null
    private var nodes: List<CachedNode> = emptyList()
    private var selectedNodeId: String? = null
    private var arrival: Date? = null
    private var slotOptions: List<SlotOption> = emptyList()
    private var slotCall: Call<ApiResponse<List<BatterySlotDto>>>? = null

    private lateinit var spNode: Spinner
    private lateinit var spSlot: Spinner
    private lateinit var btnPickTime: TextView
    private lateinit var btnSubmit: Button
    private lateinit var rbCharging: RadioButton
    private lateinit var rbDropOff: RadioButton
    private lateinit var tvError: TextView

    // Binds the views and sets the screen up for Create or Modify
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        supportActionBar?.hide()
        setContentView(R.layout.activity_reservation_form)

        spNode = findViewById(R.id.spNode)
        spSlot = findViewById(R.id.spSlot)
        btnPickTime = findViewById(R.id.btnPickTime)
        btnSubmit = findViewById(R.id.btnSubmitReservation)
        rbCharging = findViewById(R.id.rbCharging)
        rbDropOff = findViewById(R.id.rbDropOff)
        tvError = findViewById(R.id.tvFormError)

        editing = intent.getStringExtra(EXTRA_RESERVATION)?.let {
            Gson().fromJson(it, ReservationDto::class.java)
        }

        val title = findViewById<TextView>(R.id.tvFormTitle)
        val subtitle = findViewById<TextView>(R.id.tvFormSubtitle)
        val current = editing
        if (current == null) {
            title.text = "Reserve an energy slot"
            subtitle.text = "Pick a node and your arrival time to see free battery slots."
            btnSubmit.text = "Request reservation"
        } else {
            title.text = "Modify reservation"
            subtitle.text = "${current.id.takeLast(8).uppercase()} · change the time, slot or type."
            btnSubmit.text = "Save changes"
            arrival = ReservationTime.parse(current.scheduledDateTime)
            if (current.type.equals("EnergyDropOff", true)) rbDropOff.isChecked = true else rbCharging.isChecked = true
            findViewById<TextView>(R.id.tvNodeLocked).visibility = View.VISIBLE
        }

        findViewById<Button>(R.id.btnFormBack).setOnClickListener { finish() }
        btnPickTime.setOnClickListener { pickDateTime() }
        btnSubmit.setOnClickListener { submit() }
        spNode.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedNodeId = if (position == 0) null else nodes.getOrNull(position - 1)?.id
                loadSlots()
            }
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }

        updateTimeButton()
        setSlots(emptyList(), "Pick a node and time first")
        loadNodes()
    }

    // Loads nodes from the API (cached in SQLite) into the node dropdown
    private fun loadNodes() {
        NodeDirectory.refresh(this) {
            if (isFinishing) return@refresh
            nodes = NodeDirectory.all(this)
            val current = editing
            // Keep the booked node selectable even if it is no longer listed
            if (current != null && nodes.none { it.id == current.nodeId }) {
                nodes = listOf(CachedNode(current.nodeId, NodeDirectory.nameFor(this, current.nodeId))) + nodes
            }
            val labels = listOf("Select a node") + nodes.map { it.name }
            spNode.adapter = ArrayAdapter(this, R.layout.item_spinner, labels).apply {
                setDropDownViewResource(R.layout.item_spinner_dropdown)
            }
            if (current != null) {
                spNode.setSelection(nodes.indexOfFirst { it.id == current.nodeId } + 1)
                spNode.isEnabled = false // node cannot change on an existing booking
            }
        }
    }

    // Date picker, then time picker (limited to the next 7 days as a hint; server enforces it)
    private fun pickDateTime() {
        val cal = Calendar.getInstance()
        arrival?.let { cal.time = it }
        val dateDialog = DatePickerDialog(this, { _, year, month, day ->
            TimePickerDialog(this, { _, hour, minute ->
                cal.set(year, month, day, hour, minute, 0)
                arrival = cal.time
                updateTimeButton()
                loadSlots()
            }, cal.get(Calendar.HOUR_OF_DAY), cal.get(Calendar.MINUTE), true).show()
        }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH), cal.get(Calendar.DAY_OF_MONTH))
        val now = System.currentTimeMillis()
        dateDialog.datePicker.minDate = now - 1000
        dateDialog.datePicker.maxDate = now + 7L * 24 * 60 * 60 * 1000
        dateDialog.show()
    }

    // Shows the chosen time on the button
    private fun updateTimeButton() {
        btnPickTime.text = arrival?.let { ReservationTime.dayTime(it) } ?: "Choose date & time"
    }

    // GET /api/nodes/{id}/battery-slots/available?arrivalTime=...
    private fun loadSlots() {
        slotCall?.cancel()
        val nodeId = selectedNodeId
        val time = arrival
        if (nodeId == null || time == null) {
            setSlots(emptyList(), "Pick a node and time first")
            return
        }
        setSlots(emptyList(), "Loading slots...")
        val call = ApiClient.getClient(applicationContext).create(NodeApi::class.java)
            .getAvailableSlots(nodeId, ReservationTime.toIsoUtc(time))
        slotCall = call
        call.enqueue(object : Callback<ApiResponse<List<BatterySlotDto>>> {
            override fun onResponse(
                c: Call<ApiResponse<List<BatterySlotDto>>>,
                r: Response<ApiResponse<List<BatterySlotDto>>>
            ) {
                if (isFinishing || c.isCanceled) return
                val options = r.body()?.data.orEmpty().map { slot ->
                    val start = ReservationTime.time(ReservationTime.parse(slot.startTime))
                    val end = ReservationTime.time(ReservationTime.parse(slot.endTime))
                    SlotOption(slot.id, "$start – $end")
                }.toMutableList()
                // Modify: the booking's own slot is "Booked", so offer it as the current choice
                val current = editing
                if (current != null && current.nodeId == nodeId && options.none { it.id == current.batterySlotId }) {
                    options.add(0, SlotOption(current.batterySlotId, "Current slot"))
                }
                setSlots(options, if (r.isSuccessful) "No free slots at this time" else "Could not load slots")
            }

            override fun onFailure(c: Call<ApiResponse<List<BatterySlotDto>>>, t: Throwable) {
                if (isFinishing || c.isCanceled) return
                setSlots(emptyList(), "Could not reach the server")
            }
        })
    }

    // Fills the slot dropdown (or shows a placeholder when there are none)
    private fun setSlots(options: List<SlotOption>, placeholder: String) {
        slotOptions = options
        val labels = if (options.isEmpty()) listOf(placeholder) else options.map { it.label }
        spSlot.adapter = ArrayAdapter(this, R.layout.item_spinner, labels).apply {
            setDropDownViewResource(R.layout.item_spinner_dropdown)
        }
        spSlot.isEnabled = options.isNotEmpty()
        val keep = options.indexOfFirst { it.id == editing?.batterySlotId }
        if (keep >= 0) spSlot.setSelection(keep)
    }

    // Sends Create (POST) or Modify (PUT) and opens the Booking Summary on success
    private fun submit() {
        tvError.visibility = View.GONE
        val nodeId = selectedNodeId
        val time = arrival
        val slotId = slotOptions.getOrNull(spSlot.selectedItemPosition)?.id
        if (nodeId == null || time == null || slotId == null) {
            showError("Please pick a node, a time and an available battery slot.")
            return
        }
        val type = if (rbDropOff.isChecked) "EnergyDropOff" else "Charging"
        val api = ApiClient.getClient(applicationContext).create(ReservationApi::class.java)
        val current = editing
        val call = if (current == null) {
            api.create(CreateReservationRequest(nodeId, slotId, type, ReservationTime.toIsoUtc(time)))
        } else {
            api.update(current.id, UpdateReservationRequest(slotId, ReservationTime.toIsoUtc(time), type))
        }
        val action = if (current == null) BookingSummaryActivity.ACTION_CREATED else BookingSummaryActivity.ACTION_UPDATED

        btnSubmit.isEnabled = false
        call.enqueue(object : Callback<ReservationDto> {
            override fun onResponse(c: Call<ReservationDto>, r: Response<ReservationDto>) {
                btnSubmit.isEnabled = true
                val saved = r.body()
                if (r.isSuccessful && saved != null) {
                    startActivity(BookingSummaryActivity.intent(this@ReservationFormActivity, saved, action))
                    finish()
                } else {
                    // e.g. "Reservations must be scheduled within the next 7 days."
                    showError(apiErrorMessage(r, "Could not save the reservation (HTTP ${r.code()})."))
                }
            }

            override fun onFailure(c: Call<ReservationDto>, t: Throwable) {
                btnSubmit.isEnabled = true
                showError(t.message ?: "Could not reach the server.")
            }
        })
    }

    // Shows an error banner above the form
    private fun showError(message: String) {
        tvError.text = message
        tvError.visibility = View.VISIBLE
    }

    // Cancels a slot request still running when the screen closes
    override fun onDestroy() {
        slotCall?.cancel()
        super.onDestroy()
    }
}
