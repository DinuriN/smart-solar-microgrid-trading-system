/*
 * File Name    : ReservationModels.kt
 * Description  : Data classes matching the JSON sent and returned by the
 *                Reservation and Node endpoints of the SmartGrid Web API.
 * Author       : Kandaudahewa C I
 * IT Number    : IT23453142
 * Date         : 2026-09-28
 */
package com.example.solaragrid.models

// Mirrors ReservationResponseDto.cs (returned by every reservation endpoint)
data class ReservationDto(
    val id: String,
    val prosumerNic: String?,
    val nodeId: String,
    val batterySlotId: String,
    val type: String,               // "Charging" | "EnergyDropOff"
    val scheduledDateTime: String,  // ISO-8601, UTC
    val status: String,             // Pending | Approved | Blocked | Completed | Cancelled
    val qrCode: String?,
    val qrGeneratedAt: String?,
    val createdAt: String?
)

// Mirrors ReservationCountsDto.cs (GET /api/reservations/counts/{nic})
data class ReservationCountsDto(
    val activeCount: Int,
    val pendingCount: Int
)

// Mirrors CreateReservationDto.cs. The prosumer's NIC is taken from the JWT on the server.
data class CreateReservationRequest(
    val nodeId: String,
    val batterySlotId: String,
    val type: String,
    val scheduledDateTime: String
)

// Mirrors UpdateReservationDto.cs. The node cannot be changed on update.
data class UpdateReservationRequest(
    val batterySlotId: String?,
    val scheduledDateTime: String?,
    val type: String?
)

// Only the SolarMicroGrid fields this component needs (GET /api/nodes)
data class NodeDto(
    val id: String,
    val nodeName: String,
    val isActive: Boolean
)

// Mirrors BatterySlot.cs (GET /api/nodes/{id}/battery-slots/available)
data class BatterySlotDto(
    val id: String,
    val microGridId: String,
    val startTime: String,
    val endTime: String,
    val status: String
)
