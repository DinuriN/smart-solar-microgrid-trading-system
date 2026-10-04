package com.example.solaragrid.api

import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class VerifyQrRequest(val qrCode: String)

data class VerifyQrResponse(
    val verified: Boolean = false,
    val reservationId: String? = null,
    val message: String? = null,
    val alreadyVerified: Boolean = false,
    val prosumerNic: String? = null,
    val nodeId: String? = null,
    val batterySlotId: String? = null,
    val type: String? = null,
    val scheduledDateTime: String? = null,
    val status: String? = null
)

data class FinalizeTransferRequest(val reservationId: String)

data class FinalizeTransferResponse(
    val success: Boolean = false,
    val message: String? = null
)

interface GridOperatorQrApi {
    @POST("api/qr/verify")
    fun verify(@Body request: VerifyQrRequest): Call<VerifyQrResponse>

    @POST("api/qr/finalize-transfer")
    fun finalizeTransfer(
        @Body request: FinalizeTransferRequest
    ): Call<FinalizeTransferResponse>
}