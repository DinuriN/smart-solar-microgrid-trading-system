package com.example.solaragrid.api

import com.example.solaragrid.models.ApiResponse
import com.example.solaragrid.models.ProsumerProfile
import com.example.solaragrid.models.UpdateProsumerDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.PUT
import retrofit2.http.Path

interface ProsumerService {

    @GET("api/prosumers/{nic}")
    fun getProfile(@Path("nic") nic: String): Call<ApiResponse<ProsumerProfile>>

    @PUT("api/prosumers/{nic}/profile")
    fun updateProfile(
        @Path("nic") nic: String,
        @Body request: UpdateProsumerDto
    ): Call<ApiResponse<Any>>

    @PATCH("api/prosumers/{nic}/request-deactivation")
    fun requestDeactivation(@Path("nic") nic: String): Call<ApiResponse<Any>>
}
