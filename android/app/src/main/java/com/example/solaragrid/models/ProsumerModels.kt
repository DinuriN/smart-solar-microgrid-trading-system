package com.example.solaragrid.models

// The C# UpdateProsumerDto only takes Name, Phone, Address
data class UpdateProsumerDto(
    val name: String,
    val phone: String,
    val address: String
)

// Prosumer full profile from GET /api/prosumers/{nic}
data class ProsumerProfile(
    val nic: String,
    val name: String,
    val email: String,
    val phone: String,
    val address: String,
    val isActive: Boolean
)
