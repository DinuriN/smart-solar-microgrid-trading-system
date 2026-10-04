package com.example.solaragrid.api

import com.example.solaragrid.models.ApiResponse
import com.example.solaragrid.models.SolarMicroGrid
import retrofit2.Call
import retrofit2.http.GET

interface NodeService {
    // Fetches all active nodes (your backend returns all active nodes with coordinates)
    @GET("api/nodes")
    fun getAllNodes(): Call<ApiResponse<List<SolarMicroGrid>>>
}