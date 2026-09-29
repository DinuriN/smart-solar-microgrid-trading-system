package com.example.solaragrid.api

import com.example.solaragrid.models.ApiResponse
import com.example.solaragrid.models.AuthResponseDto
import com.example.solaragrid.models.LoginDto
import com.example.solaragrid.models.RegisterProsumerDto
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

// Retrofit interface defining the exact HTTP endpoints in our C# API
interface AuthService {

    // POST http://10.0.2.2:5000/api/auth/login
    @POST("api/auth/login")
    fun login(@Body request: LoginDto): Call<ApiResponse<AuthResponseDto>>

    // POST http://10.0.2.2:5000/api/prosumers/register
    @POST("api/prosumers/register")
    fun registerProsumer(@Body request: RegisterProsumerDto): Call<ApiResponse<Any>>
}
