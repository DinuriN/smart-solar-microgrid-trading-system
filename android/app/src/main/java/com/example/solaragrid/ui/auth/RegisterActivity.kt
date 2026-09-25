package com.example.solaragrid.ui.auth

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.solaragrid.R
import com.example.solaragrid.api.ApiClient
import com.example.solaragrid.api.AuthService
import com.example.solaragrid.models.ApiResponse
import com.example.solaragrid.models.RegisterProsumerDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RegisterActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Hide top action bar for custom UI
        supportActionBar?.hide() 
        
        setContentView(R.layout.activity_register)

        // Find all 6 input fields
        val etNic = findViewById<EditText>(R.id.etNic)
        val etName = findViewById<EditText>(R.id.etName)
        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPhone = findViewById<EditText>(R.id.etPhone)
        val etAddress = findViewById<EditText>(R.id.etAddress)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        
        val btnRegister = findViewById<Button>(R.id.btnRegister)
        val tvLoginNow = findViewById<TextView>(R.id.tvLoginNow)

        btnRegister.setOnClickListener {
            val nic = etNic.text.toString().trim()
            val name = etName.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val phone = etPhone.text.toString().trim()
            val address = etAddress.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (nic.isEmpty() || name.isEmpty() || email.isEmpty() || phone.isEmpty() || address.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1. Create our Data Object to send to C#
            val request = RegisterProsumerDto(nic, name, email, phone, address, password)
            
            // 2. Use Retrofit to make the API Call
            val authService = ApiClient.getClient(this).create(AuthService::class.java)
            authService.registerProsumer(request).enqueue(object : Callback<ApiResponse<Any>> {
                override fun onResponse(call: Call<ApiResponse<Any>>, response: Response<ApiResponse<Any>>) {
                    if (response.isSuccessful) {
                        Toast.makeText(this@RegisterActivity, "Registration Successful! Please login.", Toast.LENGTH_LONG).show()
                        finish() // Go back to login
                    } else {
                        Toast.makeText(this@RegisterActivity, "Registration Failed: ${response.code()}", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable) {
                    Toast.makeText(this@RegisterActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }

        // Return to Login Screen if they click the bottom link
        tvLoginNow.setOnClickListener {
            // Because RegisterActivity was launched FROM LoginActivity, 
            // calling finish() simply destroys this screen and reveals Login underneath!
            finish()
        }
    }
}
