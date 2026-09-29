package com.example.solaragrid.ui.dashboard

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.solaragrid.R
import com.example.solaragrid.api.ApiClient
import com.example.solaragrid.api.NodeService
import com.example.solaragrid.models.ApiResponse
import com.example.solaragrid.models.SolarMicroGrid
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private var progressBar: ProgressBar? = null

    // Default center (Colombo, Sri Lanka) just in case GPS fails
    private val defaultLocation = LatLng(6.9271, 79.8612)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_map, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        progressBar = view.findViewById(R.id.progressBar)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        // 1. Try to center map on user's location
        getUserLocation()

        // 2. Fetch and plot all active nodes from your backend
        fetchAndPlotNodes()
    }

    private fun getUserLocation() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // Request permissions if not granted
            requestPermissions(
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
                100
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val userLatLng = LatLng(location.latitude, location.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 12f))

                // Add a blue marker for the user
                mMap.addMarker(
                    MarkerOptions()
                        .position(userLatLng)
                        .title("You are here")
                        .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                )
            } else {
                // Fallback to default location
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
            }
        }
    }

    private fun fetchAndPlotNodes() {
        progressBar?.visibility = View.VISIBLE

        val service = ApiClient.getClient(requireContext()).create(NodeService::class.java)
        service.getAllNodes().enqueue(object : Callback<ApiResponse<List<SolarMicroGrid>>> {
            override fun onResponse(
                call: Call<ApiResponse<List<SolarMicroGrid>>>,
                response: Response<ApiResponse<List<SolarMicroGrid>>>
            ) {
                progressBar?.visibility = View.GONE
                if (response.isSuccessful && response.body()?.success == true) {
                    val nodes = response.body()?.data ?: emptyList()
                    plotNodes(nodes)
                } else {
                    Toast.makeText(context, "Failed to load nodes", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse<List<SolarMicroGrid>>>, t: Throwable) {
                progressBar?.visibility = View.GONE
                Toast.makeText(context, "Network Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun plotNodes(nodes: List<SolarMicroGrid>) {
        for (node in nodes) {
            val latLng = LatLng(node.location.latitude, node.location.longitude)
            // Green for active, Red for inactive
            val color = if (node.isActive) BitmapDescriptorFactory.HUE_GREEN else BitmapDescriptorFactory.HUE_RED

            mMap.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title(node.nodeName)
                    .snippet("Capacity: ${node.capacityKWh} kWh\n${node.location.address ?: "No address"}")
                    .icon(BitmapDescriptorFactory.defaultMarker(color))
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 100 && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            getUserLocation()
        }
    }
}