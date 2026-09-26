package com.example.solaragrid.api

import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

data class ReservationQrDto(
    val id: String,
    val status: String,
    val scheduledDateTime: String?,
    val qrCode: String?
)

interface ProsumerQrApi {
    // The search endpoint scopes a Prosumer to the NIC in their authenticated JWT.
    @GET("api/reservations/search")
    fun getApprovedReservations(
        @Query("criteria") nic: String,
        @Query("status") status: String
    ): Call<List<ReservationQrDto>>
}
