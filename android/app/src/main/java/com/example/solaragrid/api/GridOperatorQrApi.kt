package com.example.solaragrid.api


import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.POST

data class VerifyQrRequest(val qrCode: String)
data class VerifyQrResponse(
    val verified: Boolean,
    val reservationId: String?,
    val message: String?
)

data class FinalizeTransferRequest(val reservationId: String)
data class FinalizeTransferResponse(
    val success: Boolean,
    val message: String?
)

interface GridOperatorQrApi {
    @POST("api/qr/verify")
    fun verify(@Body request: VerifyQrRequest): Call<VerifyQrResponse>

    @POST("api/qr/finalize-transfer")
    fun finalizeTransfer(
        @Body request: FinalizeTransferRequest
    ): Call<FinalizeTransferResponse>
}