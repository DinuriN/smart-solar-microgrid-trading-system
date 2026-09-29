/*
 * File Name    : NodeApi.kt
 * Description  : Retrofit interface for the Microgrid Node endpoints (Member 2's API)
 *                used by the reservation screens: node list and free battery slots.
 * Author       : Kandaudahewa C I
 * IT Number    : IT23453142
 * Date         : 2026-09-28
 */
package com.example.solaragrid.api

import com.example.solaragrid.models.ApiResponse
import com.example.solaragrid.models.BatterySlotDto
import com.example.solaragrid.models.NodeDto
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface NodeApi {

    // All active microgrid nodes, wrapped as { success, data }
    @GET("api/nodes")
    fun getNodes(): Call<ApiResponse<List<NodeDto>>>

    // Battery slots of a node that are free at the given arrival time (ISO-8601 UTC)
    @GET("api/nodes/{id}/battery-slots/available")
    fun getAvailableSlots(
        @Path("id") nodeId: String,
        @Query("arrivalTime") arrivalTime: String
    ): Call<ApiResponse<List<BatterySlotDto>>>
}
