package com.example.studyspotfinder.data.repository

import com.example.studyspotfinder.data.model.Spot
import com.google.firebase.firestore.FirebaseFirestore

class SpotRepository {

    private val firestore = FirebaseFirestore.getInstance()

    fun getAllSpots(onSuccess: (List<Spot>) -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("spots").get()
            .addOnSuccessListener { documents ->
                val spots = documents.map { doc ->
                    val spot = doc.toObject(Spot::class.java)
                    spot.copy(id = doc.id)  // Set Firestore document ID in Spot
                }
                onSuccess(spots)
            }
            .addOnFailureListener { e -> onFailure(e) }
    }

    fun addSpot(spot: Spot, onSuccess: () -> Unit, onFailure: (Exception) -> Unit) {
        firestore.collection("spots").add(spot)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onFailure(e) }
    }
}
