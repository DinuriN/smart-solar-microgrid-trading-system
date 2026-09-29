package com.example.solaragrid.models

data class SolarMicroGrid(
    val id: String,
    val nodeName: String,
    val location: GeoLocation,
    val capacityKWh: Double,
    val isActive: Boolean
)

data class GeoLocation(
    val latitude: Double,
    val longitude: Double,
    val address: String?
)