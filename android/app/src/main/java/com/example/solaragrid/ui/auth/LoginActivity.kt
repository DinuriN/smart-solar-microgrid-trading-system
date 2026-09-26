package com.example.solaragrid.ui.auth

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import android.util.Base64
import androidx.appcompat.app.AppCompatActivity
import com.example.solaragrid.R
import com.example.solaragrid.api.ApiClient
import com.example.solaragrid.api.AuthService
import com.example.solaragrid.database.UserManager
import com.example.solaragrid.models.ApiResponse
import com.example.solaragrid.models.AuthResponseDto
import com.example.solaragrid.models.LoginDto
import org.json.JSONObject
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import android.content.Intent
import com.example.solaragrid.ui.operator.GridOperatorScanActivity

class LoginActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Hide the default Android top bar to match your dark custom UI
        supportActionBar?.hide() 
        
        setContentView(R.layout.activity_login)

        // Find our UI elements by their IDs from the XML
        val etNic = findViewById<EditText>(R.id.etNic)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val tvCreateAccount = findViewById<TextView>(R.id.tvCreateAccount)

        // Set click listener for the Login Button
        btnLogin.setOnClickListener {
            val nic = etNic.text.toString().trim()
            val password = etPassword.text.toString().trim()

            // Basic validation
            if (nic.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter both NIC and Password", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1. Create the Login Data Object
            val request = LoginDto(nic, password)
            
            // 2. Call the C# Backend via Retrofit
            val authService = ApiClient.getClient(this).create(AuthService::class.java)
            authService.login(request).enqueue(object : Callback<ApiResponse<AuthResponseDto>> {
                override fun onResponse(call: Call<ApiResponse<AuthResponseDto>>, response: Response<ApiResponse<AuthResponseDto>>) {
                    if (response.isSuccessful && response.body()?.success == true) {
                        val authData = response.body()?.data
                        if (authData != null) {
                            
                            // 3. Decode the JWT payload to get the true NIC
                            var extractedNic = nic
                            try {
                                val split = authData.token.split(".")
                                if (split.size > 1) {
                                    val payload = String(Base64.decode(split[1], Base64.URL_SAFE))
                                    val jsonObj = JSONObject(payload)
                                    if (jsonObj.has("nic")) {
                                        extractedNic = jsonObj.getString("nic")
                                    }
                                }
                            } catch (e: Exception) { e.printStackTrace() }
                            
                            // 4. Save to our local SQLite Database (UserManager)
                            UserManager(this@LoginActivity).saveUser(authData.token, authData.role, authData.name, extractedNic)
                            
                            Toast.makeText(this@LoginActivity, "Login Successful! Welcome, ${authData.name}", Toast.LENGTH_LONG).show()

                            if (authData.role.equals("GridOperator", ignoreCase = true)) {
                                startActivity(
                                    Intent(this@LoginActivity, GridOperatorScanActivity::class.java)
                                )
                                finish()
                            }
                        }
                    } else {
                        Toast.makeText(this@LoginActivity, "Login Failed. Invalid credentials.", Toast.LENGTH_LONG).show()
                    }
                }

                override fun onFailure(call: Call<ApiResponse<AuthResponseDto>>, t: Throwable) {
                    Toast.makeText(this@LoginActivity, "Network Error: ${t.message}", Toast.LENGTH_LONG).show()
                }
            })
        }

        // Set click listener for the "Create an account" link
        tvCreateAccount.setOnClickListener {
            val intent = android.content.Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}
