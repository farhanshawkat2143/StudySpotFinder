package com.example.studyspotfinder.data.model

data class Spot(
    val id: String = "",
    val name: String = "",
    val description: String = "",
    val lat: Double = 0.0,
    val lng: Double = 0.0,
    val addedBy: String = "",
    val wifiSpeed: String? = null,        // e.g. "High", "Medium", "Low"
    val noiseLevel: String? = null,       // e.g. "Quiet", "Moderate", "Noisy"
    val seating: String? = null,          // e.g. "Few", "Moderate", "Plenty"
    val powerOutlets: Boolean? = null,
    val rating: Double? = null,
    val timestamp: com.google.firebase.Timestamp? = null
)
