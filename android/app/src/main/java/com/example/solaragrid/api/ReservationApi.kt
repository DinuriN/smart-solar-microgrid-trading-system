/*
 * File Name    : ReservationApi.kt
 * Description  : Retrofit interface for the Reservation endpoints. The server owns all
 *                business rules (7-day window, 12-hour notice, slot availability).
 * Author       : Kandaudahewa C I
 * IT Number    : IT23453142
 * Date         : 2026-09-28
 */
package com.example.solaragrid.api

import com.example.solaragrid.models.CreateReservationRequest
import com.example.solaragrid.models.ReservationCountsDto
import com.example.solaragrid.models.ReservationDto
import com.example.solaragrid.models.UpdateReservationRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ReservationApi {

    // Full booking history of the logged-in prosumer, newest first
    @GET("api/reservations/history/{nic}")
    fun getHistory(@Path("nic") nic: String): Call<List<ReservationDto>>

    // Active (Approved) and Pending counts for the dashboard
    @GET("api/reservations/counts/{nic}")
    fun getCounts(@Path("nic") nic: String): Call<ReservationCountsDto>

    // Next upcoming bookings of the logged-in prosumer; the server filters, sorts and limits them
    @GET("api/reservations/upcoming")
    fun getUpcoming(): Call<List<ReservationDto>>

    // Server-side search (NIC, node name, node ID, slot ID or reservation ID), scoped to the caller
    @GET("api/reservations/search")
    fun search(@Query("criteria") criteria: String): Call<List<ReservationDto>>

    // Creates a new reservation (saved as Pending)
    @POST("api/reservations")
    fun create(@Body body: CreateReservationRequest): Call<ReservationDto>

    // Modifies slot, time or type of an existing reservation
    @PUT("api/reservations/{id}")
    fun update(@Path("id") id: String, @Body body: UpdateReservationRequest): Call<ReservationDto>

    // Cancels a reservation (204 No Content on success)
    @DELETE("api/reservations/{id}")
    fun cancel(@Path("id") id: String): Call<Void>
}