package com.example.solaragrid.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.solaragrid.R
import com.example.solaragrid.api.ApiClient
import com.example.solaragrid.api.ProsumerService
import com.example.solaragrid.database.UserManager
import com.example.solaragrid.models.ApiResponse
import com.example.solaragrid.models.ProsumerProfile
import com.example.solaragrid.models.UpdateProsumerDto
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProsumerProfileFragment : Fragment() {

    private lateinit var etName: EditText
    private lateinit var etNic: EditText
    private lateinit var etAddress: EditText
    private lateinit var etPhone: EditText
    private lateinit var btnSave: Button
    private lateinit var btnDeactivate: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_prosumer_profile, container, false)
        
        etName = view.findViewById(R.id.etName)
        etNic = view.findViewById(R.id.etNic)
        etAddress = view.findViewById(R.id.etAddress)
        etPhone = view.findViewById(R.id.etPhone)
        btnSave = view.findViewById(R.id.btnSave)
        btnDeactivate = view.findViewById(R.id.btnDeactivate)
        val btnLogout = view.findViewById<Button>(R.id.btnLogout)

        // Get NIC from SQLite
        val user = UserManager(requireContext()).getLoggedInUser()
        val nic = user?.nic ?: ""

        // Fetch Profile
        fetchProfile(nic)

        // Edit / Save Toggle Logic
        btnSave.setOnClickListener {
            if (btnSave.text.toString() == getString(R.string.btn_edit_profile)) {
                // Enable fields
                etName.isEnabled = true
                etAddress.isEnabled = true
                etPhone.isEnabled = true
                btnSave.text = getString(R.string.btn_save_changes)
            } else {
                // Perform Save
                val updateDto = UpdateProsumerDto(
                    name = etName.text.toString(),
                    phone = etPhone.text.toString(),
                    address = etAddress.text.toString()
                )
                updateProfile(nic, updateDto)
            }
        }

        // Deactivation
        btnDeactivate.setOnClickListener {
            requestDeactivation(nic)
        }

        // Logout
        btnLogout.setOnClickListener {
            // 1. Clear the SQLite DB
            UserManager(requireContext()).clearUser()
            
            // 2. Route back to Login Screen and clear the activity backstack
            val intent = android.content.Intent(requireContext(), com.example.solaragrid.ui.auth.LoginActivity::class.java)
            intent.flags = android.content.Intent.FLAG_ACTIVITY_NEW_TASK or android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }

        return view
    }

    private fun fetchProfile(nic: String) {
        val service = ApiClient.getClient(requireContext()).create(ProsumerService::class.java)
        service.getProfile(nic).enqueue(object : Callback<ApiResponse<ProsumerProfile>> {
            override fun onResponse(call: Call<ApiResponse<ProsumerProfile>>, response: Response<ApiResponse<ProsumerProfile>>) {
                if (response.isSuccessful && response.body()?.success == true) {
                    val profile = response.body()?.data
                    profile?.let {
                        etName.setText(it.name)
                        etNic.setText(it.nic)
                        etAddress.setText(it.address)
                        etPhone.setText(it.phone)
                    }
                } else {
                    Toast.makeText(context, "Failed to load profile", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ApiResponse<ProsumerProfile>>, t: Throwable) {
                Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateProfile(nic: String, dto: UpdateProsumerDto) {
        val service = ApiClient.getClient(requireContext()).create(ProsumerService::class.java)
        service.updateProfile(nic, dto).enqueue(object : Callback<ApiResponse<Any>> {
            override fun onResponse(call: Call<ApiResponse<Any>>, response: Response<ApiResponse<Any>>) {
                if (response.isSuccessful) {
                    Toast.makeText(context, "Profile Updated", Toast.LENGTH_SHORT).show()
                    // Disable fields again
                    etName.isEnabled = false
                    etAddress.isEnabled = false
                    etPhone.isEnabled = false
                    btnSave.text = getString(R.string.btn_edit_profile)
                } else {
                    Toast.makeText(context, "Update Failed", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable) {
                Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun requestDeactivation(nic: String) {
        val service = ApiClient.getClient(requireContext()).create(ProsumerService::class.java)
        service.requestDeactivation(nic).enqueue(object : Callback<ApiResponse<Any>> {
            override fun onResponse(call: Call<ApiResponse<Any>>, response: Response<ApiResponse<Any>>) {
                if (response.isSuccessful) Toast.makeText(context, "Deactivation Requested", Toast.LENGTH_LONG).show()
                else Toast.makeText(context, "Request Failed", Toast.LENGTH_SHORT).show()
            }
            override fun onFailure(call: Call<ApiResponse<Any>>, t: Throwable) {
                Toast.makeText(context, "Network Error", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
