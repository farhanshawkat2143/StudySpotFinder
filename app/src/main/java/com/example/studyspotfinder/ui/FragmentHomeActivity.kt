package com.example.studyspotfinder.ui

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.widget.EditText
import android.widget.SearchView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.studyspotfinder.R
import com.example.studyspotfinder.viewmodel.SpotViewModel
import com.example.studyspotfinder.data.model.Spot
import com.example.studyspotfinder.databinding.FragmentHomeBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*

class HomeFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var map: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var viewModel: SpotViewModel

    private val LOCATION_PERMISSION_REQUEST_CODE = 101
    private var locationPermissionGranted = false

    private val markerMap = mutableMapOf<String, Marker>()
    private var allSpots: List<Spot> = emptyList()
    private var userLocationMarker: Marker? = null

    private val filterLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data = result.data
            val wifi = data?.getStringExtra("wifiSpeed")
            val noise = data?.getStringExtra("noiseLevel")
            val seating = data?.getStringExtra("seating")
            val power = if (data?.hasExtra("powerOutlets") == true) data.getBooleanExtra("powerOutlets", false) else null

            applyFilters(wifi, noise, seating, power)
        }
    }

    override fun onCreateView(
        inflater: android.view.LayoutInflater,
        container: android.view.ViewGroup?,
        savedInstanceState: Bundle?
    ): android.view.View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: android.view.View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        viewModel = ViewModelProvider(this)[SpotViewModel::class.java]

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        binding.fabAddSpot.setOnClickListener {
            startActivity(Intent(requireContext(), AddSpotActivity::class.java))
        }

        binding.btnFilter.setOnClickListener {
            val intent = Intent(requireContext(), SearchFilterActivity::class.java)
            filterLauncher.launch(intent)
        }

        binding.searchView.isIconified = false
        binding.searchView.clearFocus()
        val searchEditText = binding.searchView.findViewById<EditText>(androidx.appcompat.R.id.search_src_text)
        searchEditText.hint = "Search Location"
        searchEditText.setHintTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))
        searchEditText.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))

        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener,
            androidx.appcompat.widget.SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                query?.let { searchSpot(it) }
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean = false
        })

        viewModel.spots.observe(viewLifecycleOwner) { spots ->
            if (::map.isInitialized) {
                allSpots = spots
                displaySpotsOnMap(spots)
            }
        }

        viewModel.error.observe(viewLifecycleOwner) { message ->
            message?.let {
                Toast.makeText(requireContext(), it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun displaySpotsOnMap(spots: List<Spot>) {
        map.clear()
        markerMap.clear()

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_marker_blue)
        val smallMarker = Bitmap.createScaledBitmap(bitmap, 96, 96, false)
        val markerIcon = BitmapDescriptorFactory.fromBitmap(smallMarker)

        spots.forEach { spot ->
            val position = LatLng(spot.lat, spot.lng)
            val marker = map.addMarker(
                MarkerOptions()
                    .position(position)
                    .title(spot.name)
                    .icon(markerIcon)
            )
            if (marker != null) {
                marker.tag = spot
                markerMap[spot.name.lowercase()] = marker
            }
        }

        // Also re-add user location marker if exists
        getDeviceLocation()
    }

    private fun applyFilters(wifi: String?, noise: String?, seating: String?, power: Boolean?) {
        val filtered = allSpots.filter { spot ->
            val wifiMatch = wifi.isNullOrEmpty() || spot.wifiSpeed?.trim()?.equals(wifi.trim(), ignoreCase = true) == true
            val noiseMatch = noise.isNullOrEmpty() || spot.noiseLevel?.trim()?.equals(noise.trim(), ignoreCase = true) == true
            val seatingMatch = seating.isNullOrEmpty() || spot.seating?.trim()?.equals(seating.trim(), ignoreCase = true) == true
            val powerMatch = power == null || (spot.powerOutlets ?: false) == power
            wifiMatch && noiseMatch && seatingMatch && powerMatch
        }

        if (filtered.isEmpty()) {
            Toast.makeText(requireContext(), "No matching results", Toast.LENGTH_SHORT).show()
        }

        displaySpotsOnMap(filtered)
    }

    private fun searchSpot(query: String) {
        val marker = markerMap[query.lowercase()]
        if (marker != null) {
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(marker.position, 17f))
            marker.showInfoWindow()
        } else {
            Toast.makeText(requireContext(), "No matching spot found", Toast.LENGTH_SHORT).show()
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        try {
            val success = map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(),
                R.raw.map_style
            ))
            if (!success) {
                Toast.makeText(requireContext(), "Failed to apply map style.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(requireContext(), "Style error: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        getLocationPermission()
        updateLocationUI()
        getDeviceLocation()
        viewModel.fetchSpots()

        map.setOnMarkerClickListener { marker ->
            val spot = marker.tag as? Spot
            spot?.let {
                val intent = Intent(requireContext(), SpotDetailsActivity::class.java).apply {
                    putExtra("spotId", spot.id)
                    putExtra("spotName", spot.name)
                    putExtra("description", spot.description)
                    putExtra("wifiSpeed", spot.wifiSpeed ?: "Unknown")
                    putExtra("seating", spot.seating ?: "Unknown")
                    putExtra("powerOutlets", spot.powerOutlets ?: false)
                    putExtra("rating", spot.rating?.toFloat() ?: 0f)
                    putExtra("lat", spot.lat)
                    putExtra("lng", spot.lng)
                    putExtra("addedBy", spot.addedBy)
                }
                startActivity(intent)
            }
            true
        }
    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    @SuppressLint("MissingPermission")
    private fun updateLocationUI() {
        if (::map.isInitialized) {
            try {
                map.isMyLocationEnabled = false // Disable default blue dot
                map.uiSettings.isMyLocationButtonEnabled = true
            } catch (e: SecurityException) {
                Toast.makeText(requireContext(), "Location UI error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    private fun getDeviceLocation() {
        if (!locationPermissionGranted) return

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                location?.let {
                    val userLatLng = LatLng(it.latitude, it.longitude)

                    // Remove previous location marker
                    userLocationMarker?.remove()

                    val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_my_location)
                    val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 80, 80, false)
                    val markerIcon = BitmapDescriptorFactory.fromBitmap(scaledBitmap)

                    userLocationMarker = map.addMarker(
                        MarkerOptions()
                            .position(userLatLng)
                            .title("You are here")
                            .icon(markerIcon)
                    )

                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                } ?: Toast.makeText(requireContext(), "Unable to fetch location", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error getting location: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onResume() {
        super.onResume()
        viewModel.fetchSpots()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
