package com.example.studyspotfinder.ui

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.location.Location
import android.os.Bundle
import android.view.MotionEvent
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.annotation.RequiresPermission
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.example.studyspotfinder.R
import com.example.studyspotfinder.viewmodel.SpotViewModel
import com.example.studyspotfinder.data.model.Spot
import com.example.studyspotfinder.databinding.ActivityAddSpotBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth

class AddSpotActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityAddSpotBinding
    private lateinit var map: GoogleMap
    private var selectedMarker: Marker? = null
    private var userLocationMarker: Marker? = null
    private lateinit var viewModel: SpotViewModel
    private val auth = FirebaseAuth.getInstance()
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private val LOCATION_PERMISSION_REQUEST_CODE = 101
    private var locationPermissionGranted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAddSpotBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this)[SpotViewModel::class.java]
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragment) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Enable smooth map interaction inside ScrollView
        mapFragment.view?.setOnTouchListener { _, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> binding.root.requestDisallowInterceptTouchEvent(true)
                MotionEvent.ACTION_UP -> binding.root.requestDisallowInterceptTouchEvent(false)
            }
            false
        }

        setupSpinners()

        binding.btnSubmit.setOnClickListener {
            submitSpot()
        }

        viewModel.error.observe(this) { message ->
            message?.let {
                Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupSpinners() {
        val wifiAdapter = ArrayAdapter.createFromResource(
            this, R.array.wifi_options, android.R.layout.simple_spinner_item
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.spinnerWifiSpeed.adapter = wifiAdapter

        val noiseAdapter = ArrayAdapter.createFromResource(
            this, R.array.noise_options, android.R.layout.simple_spinner_item
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.spinnerNoiseLevel.adapter = noiseAdapter

        val seatingAdapter = ArrayAdapter.createFromResource(
            this, R.array.seating_options, android.R.layout.simple_spinner_item
        ).apply { setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item) }
        binding.spinnerSeating.adapter = seatingAdapter
    }

    @RequiresPermission(allOf = [Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION])
    override fun onMapReady(googleMap: GoogleMap) {
        map = googleMap

        try {
            val success = map.setMapStyle(MapStyleOptions.loadRawResourceStyle(this,
                R.raw.map_style
            ))
            if (!success) {
                Toast.makeText(this, "Failed to apply map style.", Toast.LENGTH_SHORT).show()
            }
        } catch (e: Exception) {
            Toast.makeText(this, "Style error: ${e.message}", Toast.LENGTH_SHORT).show()
        }

        getLocationPermission()
        updateLocationUI()
        getDeviceLocation()

        map.setOnMapClickListener { latLng ->
            selectedMarker?.remove()

            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_marker_blue)
            val scaledIcon = Bitmap.createScaledBitmap(bitmap, 96, 96, false)
            val markerIcon = BitmapDescriptorFactory.fromBitmap(scaledIcon)

            selectedMarker = map.addMarker(
                MarkerOptions()
                    .position(latLng)
                    .title("Selected Spot")
                    .icon(markerIcon)
            )
            map.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 16f))
        }
    }

    private fun getLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true
        } else {
            ActivityCompat.requestPermissions(
                this,
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
                map.uiSettings.isMyLocationButtonEnabled = false
            } catch (e: SecurityException) {
                Toast.makeText(this, "Location UI error: ${e.message}", Toast.LENGTH_SHORT).show()
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

                    userLocationMarker?.remove()

                    val bitmap = BitmapFactory.decodeResource(resources, R.drawable.ic_my_location)
                    val scaledIcon = Bitmap.createScaledBitmap(bitmap, 80, 80, false)
                    val markerIcon = BitmapDescriptorFactory.fromBitmap(scaledIcon)

                    userLocationMarker = map.addMarker(
                        MarkerOptions()
                            .position(userLatLng)
                            .title("You are here")
                            .icon(markerIcon)
                    )

                    map.animateCamera(CameraUpdateFactory.newLatLngZoom(userLatLng, 15f))
                } ?: Toast.makeText(this, "Unable to fetch location", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error getting location: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun submitSpot() {
        val name = binding.etSpotName.text.toString().trim()
        val description = binding.etSpotDescription.text.toString().trim()
        val marker = selectedMarker

        if (name.isEmpty() || description.isEmpty()) {
            Toast.makeText(this, "Name and Description are required", Toast.LENGTH_SHORT).show()
            return
        }

        if (marker == null) {
            Toast.makeText(this, "Please select a location on the map", Toast.LENGTH_SHORT).show()
            return
        }

        val wifiSpeed = binding.spinnerWifiSpeed.selectedItem?.toString()
        val noiseLevel = binding.spinnerNoiseLevel.selectedItem?.toString()
        val seating = binding.spinnerSeating.selectedItem?.toString()
        val hasPowerOutlet = binding.switchPowerOutlets.isChecked
        val rating = binding.ratingBar.rating.toDouble()

        val spot = Spot(
            name = name,
            description = description,
            lat = marker.position.latitude,
            lng = marker.position.longitude,
            addedBy = auth.currentUser?.email ?: "guest",
            wifiSpeed = wifiSpeed,
            noiseLevel = noiseLevel,
            seating = seating,
            powerOutlets = hasPowerOutlet,
            rating = rating,
            timestamp = Timestamp.now()
        )

        viewModel.addSpot(spot) { success ->
            if (success) {
                Toast.makeText(this, "Spot added successfully!", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}
