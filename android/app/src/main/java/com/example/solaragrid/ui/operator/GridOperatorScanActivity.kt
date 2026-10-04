package com.example.solaragrid.ui.operator

import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.example.solaragrid.R
import com.example.solaragrid.api.ApiClient
import com.example.solaragrid.api.FinalizeTransferRequest
import com.example.solaragrid.api.FinalizeTransferResponse
import com.example.solaragrid.api.GridOperatorQrApi
import com.example.solaragrid.api.VerifyQrRequest
import com.example.solaragrid.api.VerifyQrResponse
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.codescanner.GmsBarcodeScannerOptions
import com.google.mlkit.vision.codescanner.GmsBarcodeScanning
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class GridOperatorScanActivity : AppCompatActivity() {

    private val api by lazy {
        ApiClient.getClient(applicationContext)
            .create(GridOperatorQrApi::class.java)
    }

    private lateinit var statusLabel: TextView
    private lateinit var statusText: TextView
    private lateinit var detailsCard: LinearLayout
    private lateinit var detailsContainer: LinearLayout
    private lateinit var finalizeButton: Button

    private var verifiedReservationId: String? = null

    private fun dp(value: Int): Int =
        (value * resources.displayMetrics.density).toInt()

    private fun appColor(id: Int): Int =
        ContextCompat.getColor(this, id)

    private fun cardBackground(): GradientDrawable =
        GradientDrawable().apply {
            setColor(appColor(R.color.bg_input))
            cornerRadius = dp(18).toFloat()
            setStroke(dp(1), appColor(R.color.stroke_input))
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val page = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(20), dp(30), dp(20), dp(36))
            setBackgroundColor(appColor(R.color.bg_dark))
        }

        page.addView(TextView(this).apply {
            text = "Verify energy transfer"
            textSize = 26f
            setTypeface(null, Typeface.BOLD)
            setTextColor(appColor(R.color.text_primary))
        })

        page.addView(TextView(this).apply {
            text = "Scan the prosumer's QR code and check the reservation before completing the transfer."
            textSize = 14f
            setTextColor(appColor(R.color.text_secondary))
            setPadding(0, dp(8), 0, dp(24))
        })

        // Scan section
        val scanCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = cardBackground()
        }

        scanCard.addView(TextView(this).apply {
            text = "1  Scan QR code"
            textSize = 17f
            setTypeface(null, Typeface.BOLD)
            setTextColor(appColor(R.color.text_primary))
        })

        scanCard.addView(TextView(this).apply {
            text = "Ask the prosumer to show the QR code for this booking."
            textSize = 13f
            setTextColor(appColor(R.color.text_secondary))
            setPadding(0, dp(7), 0, dp(14))
        })

        val scanButton = Button(this).apply {
            text = "Open QR scanner"
            isAllCaps = false
            backgroundTintList =
                ColorStateList.valueOf(appColor(R.color.solara_yellow))
            setTextColor(appColor(R.color.btn_text))
            setOnClickListener { scanQrCode() }
        }

        scanCard.addView(
            scanButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // Keep manual entry available for emulator testing.
        val manualEntry = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            visibility = View.GONE
        }

        val qrInput = EditText(this).apply {
            hint = "Paste QR code"
            inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            setSingleLine(true)
            textSize = 13f
            setTextColor(appColor(R.color.text_primary))
            setHintTextColor(appColor(R.color.text_secondary))
        }

        manualEntry.addView(qrInput)

        manualEntry.addView(Button(this).apply {
            text = "Verify entered code"
            isAllCaps = false
            backgroundTintList =
                ColorStateList.valueOf(appColor(R.color.stroke_input))
            setTextColor(appColor(R.color.text_primary))

            setOnClickListener {
                val code = qrInput.text.toString().trim()
                if (code.isBlank()) {
                    setStatus(
                        "Code needed",
                        "Paste a QR code first.",
                        Color.rgb(248, 113, 113)
                    )
                } else {
                    verifyQrCode(code)
                }
            }
        })

        scanCard.addView(TextView(this).apply {
            text = "Enter a code manually"
            textSize = 13f
            setTextColor(appColor(R.color.solara_yellow))
            setPadding(0, dp(16), 0, dp(5))
            setOnClickListener {
                manualEntry.visibility =
                    if (manualEntry.visibility == View.VISIBLE) {
                        View.GONE
                    } else {
                        View.VISIBLE
                    }
            }
        })

        scanCard.addView(manualEntry)

        page.addView(
            scanCard,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        )

        // Verification feedback
        val statusCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(17), dp(18), dp(17))
            background = cardBackground()
        }

        statusLabel = TextView(this).apply {
            text = "READY TO SCAN"
            textSize = 12f
            setTypeface(null, Typeface.BOLD)
            setTextColor(appColor(R.color.solara_yellow))
        }
        statusCard.addView(statusLabel)

        statusText = TextView(this).apply {
            text = "Scan a QR code to view its reservation."
            textSize = 14f
            setTextColor(appColor(R.color.text_primary))
            setPadding(0, dp(8), 0, 0)
        }
        statusCard.addView(statusText)

        page.addView(
            statusCard,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(16) }
        )

        // Reservation details appear after verification.
        detailsCard = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(18), dp(18), dp(18), dp(18))
            background = cardBackground()
            visibility = View.GONE
        }

        detailsCard.addView(TextView(this).apply {
            text = "2  Review reservation"
            textSize = 17f
            setTypeface(null, Typeface.BOLD)
            setTextColor(appColor(R.color.text_primary))
        })

        detailsContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(10), 0, 0)
        }
        detailsCard.addView(detailsContainer)

        finalizeButton = Button(this).apply {
            text = "Finalize energy transfer"
            isAllCaps = false
            isEnabled = false
            visibility = View.GONE
            backgroundTintList =
                ColorStateList.valueOf(appColor(R.color.solara_yellow))
            setTextColor(appColor(R.color.btn_text))
            setOnClickListener { finalizeTransfer() }
        }

        detailsCard.addView(
            finalizeButton,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(18) }
        )

        detailsCard.addView(TextView(this).apply {
            text = "Finalize only after the physical energy transfer is complete."
            textSize = 12f
            setTextColor(appColor(R.color.text_secondary))
            setPadding(0, dp(8), 0, 0)
        })

        page.addView(
            detailsCard,
            LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply { topMargin = dp(16) }
        )

        // No fillViewport property is needed here.
        val scrollView = ScrollView(this).apply {
            setBackgroundColor(appColor(R.color.bg_dark))
            addView(page)
        }

        setContentView(scrollView)
    }

    private fun setStatus(title: String, message: String, tone: Int) {
        statusLabel.text = title.uppercase()
        statusLabel.setTextColor(tone)
        statusText.text = message
    }

    private fun addDetail(label: String, value: String?) {
        val row = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(0, dp(9), 0, dp(9))
        }

        row.addView(TextView(this).apply {
            text = label
            textSize = 12f
            setTextColor(appColor(R.color.text_secondary))
        })

        row.addView(TextView(this).apply {
            text = value?.takeIf { it.isNotBlank() } ?: "—"
            textSize = 14f
            setTextColor(appColor(R.color.text_primary))
            setTextIsSelectable(true)
            setPadding(0, dp(3), 0, 0)
        })

        detailsContainer.addView(row)
    }

    private fun showReservation(result: VerifyQrResponse) {
        detailsContainer.removeAllViews()

        addDetail("Reservation ID", result.reservationId)
        addDetail("Prosumer NIC", result.prosumerNic)
        addDetail("Node ID", result.nodeId)
        addDetail("Battery slot ID", result.batterySlotId)
        addDetail("Transfer type", result.type)
        addDetail(
            "Scheduled time",
            result.scheduledDateTime
                ?.replace("T", " ")
                ?.removeSuffix("Z")
                ?.plus(" UTC")
        )
        addDetail("Reservation status", result.status)

        detailsCard.visibility = View.VISIBLE
        finalizeButton.visibility = View.VISIBLE
        finalizeButton.isEnabled = true
    }

    private fun scanQrCode() {
        val options = GmsBarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()

        GmsBarcodeScanning.getClient(this, options)
            .startScan()
            .addOnSuccessListener { barcode ->
                val code = barcode.rawValue
                if (code.isNullOrBlank()) {
                    setStatus(
                        "Scan unsuccessful",
                        "The QR code was empty. Please scan again.",
                        Color.rgb(248, 113, 113)
                    )
                } else {
                    verifyQrCode(code)
                }
            }
            .addOnFailureListener { error ->
                setStatus(
                    "Scanner error",
                    "Could not scan the QR code: ${error.message}",
                    Color.rgb(248, 113, 113)
                )
            }
    }

    private fun verifyQrCode(qrCode: String) {
        verifiedReservationId = null
        detailsCard.visibility = View.GONE
        finalizeButton.isEnabled = false

        setStatus(
            "Checking code",
            "Checking the reservation with the server...",
            appColor(R.color.solara_yellow)
        )

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
                        showReservation(result)

                        val message = if (result.alreadyVerified) {
                            "Already verified by you. Review the reservation before finalizing."
                        } else {
                            "QR code verified. Review the reservation before finalizing."
                        }

                        setStatus(
                            "Verified",
                            message,
                            Color.rgb(45, 212, 160)
                        )
                    } else {
                        setStatus(
                            "Verification unsuccessful",
                            result?.message
                                ?: errorMessage(
                                    response,
                                    "Verification failed (HTTP ${response.code()})."
                                ),
                            Color.rgb(248, 113, 113)
                        )
                    }
                }

                override fun onFailure(
                    call: Call<VerifyQrResponse>,
                    error: Throwable
                ) {
                    setStatus(
                        "Connection error",
                        "Could not reach the server: ${error.message}",
                        Color.rgb(248, 113, 113)
                    )
                }
            })
    }

    private fun finalizeTransfer() {
        val reservationId = verifiedReservationId ?: return

        finalizeButton.isEnabled = false
        setStatus(
            "Finalizing",
            "Updating the reservation and battery slot...",
            appColor(R.color.solara_yellow)
        )

        api.finalizeTransfer(FinalizeTransferRequest(reservationId))
            .enqueue(object : Callback<FinalizeTransferResponse> {
                override fun onResponse(
                    call: Call<FinalizeTransferResponse>,
                    response: Response<FinalizeTransferResponse>
                ) {
                    val result = response.body()

                    if (response.isSuccessful && result?.success == true) {
                        verifiedReservationId = null
                        finalizeButton.visibility = View.GONE
                        addDetail("Transfer result", "Completed")

                        setStatus(
                            "Transfer completed",
                            "The energy transfer has been finalized successfully.",
                            Color.rgb(45, 212, 160)
                        )
                    } else {
                        finalizeButton.isEnabled = true
                        setStatus(
                            "Could not finalize",
                            result?.message
                                ?: errorMessage(
                                    response,
                                    "Finalization failed (HTTP ${response.code()})."
                                ),
                            Color.rgb(248, 113, 113)
                        )
                    }
                }

                override fun onFailure(
                    call: Call<FinalizeTransferResponse>,
                    error: Throwable
                ) {
                    finalizeButton.isEnabled = true
                    setStatus(
                        "Connection error",
                        "Could not reach the server: ${error.message}",
                        Color.rgb(248, 113, 113)
                    )
                }
            })
    }

    private fun errorMessage(response: Response<*>, fallback: String): String {
        return try {
            val body = response.errorBody()?.string()
            if (body.isNullOrBlank()) {
                fallback
            } else {
                JSONObject(body).optString("message").ifBlank { fallback }
            }
        } catch (_: Exception) {
            fallback
        }
    }
}