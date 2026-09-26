package com.example.solaragrid.ui.operator

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.solaragrid.api.ApiClient
import com.example.solaragrid.api.FinalizeTransferRequest
import com.example.solaragrid.api.FinalizeTransferResponse
import com.example.solaragrid.api.GridOperatorQrApi
import com.example.solaragrid.api.VerifyQrRequest
import com.example.solaragrid.api.VerifyQrResponse
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.core.content.ContextCompat
import com.example.solaragrid.R
import android.text.InputType
import android.widget.EditText
import android.graphics.Typeface
import android.view.Gravity
import android.view.ViewGroup
import android.widget.FrameLayout

class GridOperatorScanActivity : AppCompatActivity() {

    private val api by lazy {
        ApiClient.getClient(applicationContext).create(GridOperatorQrApi::class.java)
    }

    private lateinit var statusText: TextView
    private lateinit var finalizeButton: Button
    private var verifiedReservationId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        statusText = TextView(this).apply {
            text = "Scan a prosumer's transaction QR code."
            textSize = 18f
            setTextColor(ContextCompat.getColor(this@GridOperatorScanActivity, R.color.text_primary))
        }

        val scanButton = Button(this).apply {
            text = "Scan QR code"
            setOnClickListener { scanQrCode() }
        }

        val qrInput = EditText(this).apply {
            hint = "Paste QR code here (emulator test)"
            inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            setSingleLine(true)
            setTextColor(ContextCompat.getColor(this@GridOperatorScanActivity, R.color.text_primary))
            setHintTextColor(ContextCompat.getColor(this@GridOperatorScanActivity, R.color.text_secondary))
        }

        val verifyEnteredButton = Button(this).apply {
            text = "Verify entered code"
            setOnClickListener {
                val code = qrInput.text.toString().trim()
                if (code.isBlank()) {
                    statusText.text = "Paste a QR code first."
                } else {
                    verifiedReservationId = null
                    finalizeButton.isEnabled = false
                    verifyQrCode(code)
                }
            }
        }

        finalizeButton = Button(this).apply {
            text = "Finalize energy transfer"
            isEnabled = false
            setOnClickListener { finalizeTransfer() }
        }

        val gap = (20 * resources.displayMetrics.density).toInt()

        val card = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(gap, gap * 2, gap, gap * 2)
            setBackgroundColor(
                ContextCompat.getColor(this@GridOperatorScanActivity, R.color.bg_input)
            )

            addView(TextView(this@GridOperatorScanActivity).apply {
                text = "Verify energy transfer"
                textSize = 22f
                gravity = Gravity.CENTER
                setTypeface(null, Typeface.BOLD)
                setTextColor(
                    ContextCompat.getColor(this@GridOperatorScanActivity, R.color.text_primary)
                )
            })

            statusText.setPadding(0, gap, 0, 0)
            addView(statusText)

            addView(scanButton, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = gap })

            addView(finalizeButton, LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = gap / 2 })
        }

        val screen = FrameLayout(this).apply {
            setBackgroundColor(
                ContextCompat.getColor(this@GridOperatorScanActivity, R.color.bg_dark)
            )
            addView(card, FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                Gravity.CENTER
            ).apply { setMargins(gap, 0, gap, 0) })
        }
        setContentView(screen)

//        setContentView(layout)
    }

    private fun scanQrCode() {
        verifiedReservationId = null
        finalizeButton.isEnabled = false

        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        GmsBarcodeScanning.getClient(this, options)
            .startScan()
            .addOnSuccessListener { barcode ->
                val qrCode = barcode.rawValue
                if (qrCode.isNullOrBlank()) {
                    statusText.text = "The QR code is empty. Please scan again."
                } else {
                    verifyQrCode(qrCode)
                }
            }
            .addOnFailureListener { error ->
                statusText.text = "Could not scan QR code: ${error.message}"
            }
    }

    private fun verifyQrCode(qrCode: String) {
        statusText.text = "Verifying QR code..."

        api.verify(VerifyQrRequest(qrCode))
            .enqueue(object : Callback<VerifyQrResponse> {
                override fun onResponse(
                    call: Call<VerifyQrResponse>,
                    response: Response<VerifyQrResponse>
                ) {
                    val result = response.body()

                    if (response.isSuccessful &&
                        result?.verified == true &&
                        !result.reservationId.isNullOrBlank()
                    ) {
                        verifiedReservationId = result.reservationId
                        finalizeButton.isEnabled = true
                        statusText.text =
                            "Verified reservation: ${result.reservationId}\n" +
                                    "Tap Finalize when the energy transfer is complete."
                    } else {
                        statusText.text =
                            result?.message ?: "Verification failed (HTTP ${response.code()})."
                    }
                }

                override fun onFailure(call: Call<VerifyQrResponse>, error: Throwable) {
                    statusText.text = "Could not reach the server: ${error.message}"
                }
            })
    }

    private fun finalizeTransfer() {
        val reservationId = verifiedReservationId ?: return
        finalizeButton.isEnabled = false
        statusText.text = "Finalizing energy transfer..."

        api.finalizeTransfer(FinalizeTransferRequest(reservationId))
            .enqueue(object : Callback<FinalizeTransferResponse> {
                override fun onResponse(
                    call: Call<FinalizeTransferResponse>,
                    response: Response<FinalizeTransferResponse>
                ) {
                    val result = response.body()

                    if (response.isSuccessful && result?.success == true) {
                        verifiedReservationId = null
                        statusText.text = "Energy transfer completed."
                    } else {
                        finalizeButton.isEnabled = true
                        statusText.text =
                            result?.message ?: "Finalization failed (HTTP ${response.code()})."
                    }
                }

                override fun onFailure(
                    call: Call<FinalizeTransferResponse>,
                    error: Throwable
                ) {
                    finalizeButton.isEnabled = true
                    statusText.text = "Could not reach the server: ${error.message}"
                }
            })
    }
}