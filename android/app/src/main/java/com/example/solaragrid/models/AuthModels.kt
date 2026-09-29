package com.example.solaragrid.models

// Generic wrapper for the backend's { success: true, data: ... } response
data class ApiResponse<T>(
    val success: Boolean,
    val data: T?,
    val message: String?
)

// The exact fields the C# AuthController expects
data class LoginDto(
    val emailOrNic: String,
    val password: String
)

// The exact fields the C# AuthController returns in 'data'
data class AuthResponseDto(
    val token: String,
    val role: String,
    val name: String
)

// The exact fields the C# ProsumersController expects
data class RegisterProsumerDto(
    val nic: String,
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val password: String
)
