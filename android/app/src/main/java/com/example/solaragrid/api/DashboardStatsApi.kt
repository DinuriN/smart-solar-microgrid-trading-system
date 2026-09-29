package com.example.solaragrid.api

import retrofit2.Call
import retrofit2.http.GET

data class DashboardStatsResponse(
    val pendingReservations: Long,
    val approvedFutureReservations: Long
)

interface DashboardStatsApi {
    @GET("api/dashboard/stats")
    fun getStats(): Call<DashboardStatsResponse>
}