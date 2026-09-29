/*
 * File Name    : BookingAdapter.kt
 * Description  : RecyclerView adapter that draws one booking card (node, time, status)
 *                with optional Modify / Cancel buttons.
 * Author       : Kandaudahewa C I
 * IT Number    : IT23453142
 * Date         : 2026-09-28
 */
package com.example.solaragrid.ui.reservation

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.solaragrid.R
import com.example.solaragrid.models.ReservationDto
class BookingAdapter(
    private val actionsEnabled: Boolean,
    private val nodeName: (String) -> String,
    private val onModify: (ReservationDto) -> Unit,
    private val onCancel: (ReservationDto) -> Unit
) : RecyclerView.Adapter<BookingAdapter.Holder>() {

    private var items: List<ReservationDto> = emptyList()

    // Holds the views of one card so they are looked up only once
    class Holder(view: View) : RecyclerView.ViewHolder(view) {
        val node: TextView = view.findViewById(R.id.tvBookingNode)
        val time: TextView = view.findViewById(R.id.tvBookingTime)
        val meta: TextView = view.findViewById(R.id.tvBookingMeta)
        val badge: TextView = view.findViewById(R.id.tvBookingStatus)
        val actions: LinearLayout = view.findViewById(R.id.bookingActions)
        val modify: Button = view.findViewById(R.id.btnModify)
        val cancel: Button = view.findViewById(R.id.btnCancel)
        val locked: TextView = view.findViewById(R.id.tvLocked)
    }

    // Replaces the list shown by the adapter
    fun submit(list: List<ReservationDto>) {
        items = list
        notifyDataSetChanged()
    }

    // Inflates one booking card
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder =
        Holder(LayoutInflater.from(parent.context).inflate(R.layout.item_booking, parent, false))

    override fun getItemCount(): Int = items.size

    // Fills one card and wires its buttons
    override fun onBindViewHolder(holder: Holder, position: Int) {
        val r = items[position]
        holder.node.text = nodeName(r.nodeId)
        holder.time.text = ReservationTime.dayTime(ReservationTime.parse(r.scheduledDateTime))
        holder.meta.text = "${r.id.takeLast(8).uppercase()} · ${typeLabel(r.type)}"
        styleBadge(holder.badge, r.status)

        // Modify / Cancel only for Current and Pending bookings
        val editable = actionsEnabled && r.tab() != BookingTab.HISTORY
        holder.actions.visibility = if (editable) View.VISIBLE else View.GONE
        val locked = editable && r.isLocked()
        holder.modify.isEnabled = !locked
        holder.cancel.isEnabled = !locked
        holder.locked.visibility = if (locked) View.VISIBLE else View.GONE
        holder.modify.setOnClickListener { onModify(r) }
        holder.cancel.setOnClickListener { onCancel(r) }
    }
}
