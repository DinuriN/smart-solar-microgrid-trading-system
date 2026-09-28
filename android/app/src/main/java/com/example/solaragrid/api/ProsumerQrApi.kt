package com.example.solaragrid.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

data class ReservationQrDto(
    val id: String,
    val status: String,
    val scheduledDateTime: String?,
    val qrCode: String?,
    val qrGeneratedAt: String?,
    val nodeId: String?,
    val batterySlotId: String?
)

data class NodeQrResponse(val data: NodeQrDetails?)
data class NodeQrDetails(val nodeName: String?)

data class SlotQrResponse(val data: SlotQrDetails?)
data class SlotQrDetails(val startTime: String?, val endTime: String?)

interface ProsumerQrApi {
    @GET("api/reservations/search")
    fun getApprovedReservations(
        @Query("criteria") nic: String,
        @Query("status") status: String
    ): Call<List<ReservationQrDto>>

    @GET("api/nodes/{id}")
    fun getNode(@Path("id") id: String): Call<NodeQrResponse>

    @GET("api/nodes/{nodeId}/battery-slots/{slotId}")
    fun getSlot(
        @Path("nodeId") nodeId: String,
        @Path("slotId") slotId: String
    ): Call<SlotQrResponse>

    @GET("api/reservations/search")
    fun getMyReservations(
        @Query("criteria") nic: String
    ): Call<List<ReservationQrDto>>
}

