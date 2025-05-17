package com.example.studyspotfinder.ui

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.studyspotfinder.databinding.ActivitySpotDetailsBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SpotDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySpotDetailsBinding
    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var spotId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySpotDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        spotId = intent.getStringExtra("spotId")
        if (spotId == null) {
            Toast.makeText(this, "No spot data found", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        loadSpotDetails(spotId!!)

        binding.btnFavorite.setOnClickListener {
            addToFavorites(spotId!!)
        }
    }

    private fun loadSpotDetails(id: String) {
        firestore.collection("spots").document(id).get()
            .addOnSuccessListener { doc ->
                if (doc != null && doc.exists()) {
                    val name = doc.getString("name") ?: "Unknown"
                    val description = doc.getString("description") ?: "No description available"
                    val wifiSpeed = doc.getString("wifiSpeed") ?: "Unknown"
                    val seating = doc.getString("seating") ?: "Unknown"
                    val powerOutlets = doc.getBoolean("powerOutlets") ?: false
                    val rating = doc.getDouble("rating") ?: 0.0

                    // Bind data to UI
                    binding.tvSpotName.text = name
                    binding.tvDescription.text = description
                    binding.ratingBar.rating = rating.toFloat()
                    binding.tagWifi.text = "WiFi: $wifiSpeed"
                    binding.tagSeating.text = "Seating: $seating"
                    binding.tagPower.text = "Power: ${if (powerOutlets) "Yes" else "No"}"
                } else {
                    Toast.makeText(this, "Spot not found", Toast.LENGTH_SHORT).show()
                    finish()
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Error loading spot: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun addToFavorites(spotId: String) {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(this, "Please login to add favorites", Toast.LENGTH_SHORT).show()
            return
        }

        val favoritesRef = firestore.collection("users")
            .document(user.uid)
            .collection("favorites")
            .document(spotId)

        val favoriteData = mapOf("addedAt" to com.google.firebase.Timestamp.now())

        favoritesRef.set(favoriteData)
            .addOnSuccessListener {
                Toast.makeText(this, "Added to Favorites", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to add: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }
}
