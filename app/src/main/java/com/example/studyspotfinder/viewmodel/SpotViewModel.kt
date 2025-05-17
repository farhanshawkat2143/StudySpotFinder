package com.example.studyspotfinder.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.studyspotfinder.data.model.Spot
import com.example.studyspotfinder.data.repository.SpotRepository

class SpotViewModel : ViewModel() {

    private val repository = SpotRepository()

    // LiveData to hold the list of spots
    private val _spots = MutableLiveData<List<Spot>>()
    val spots: LiveData<List<Spot>> get() = _spots

    // LiveData to hold any error messages
    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> get() = _error

    // Fetch spots from repository
    fun fetchSpots() {
        repository.getAllSpots(
            onSuccess = { spotList ->
                // Update LiveData with fetched spots
                _spots.value = spotList
                _error.value = null
            },
            onFailure = { e ->
                // Update error LiveData with error message
                _error.value = e.message
            }
        )
    }

    // Add a new spot
    fun addSpot(spot: Spot, onComplete: (Boolean) -> Unit) {
        repository.addSpot(
            spot,
            onSuccess = { onComplete(true) },
            onFailure = { e ->
                _error.value = e.message
                onComplete(false)
            }
        )
    }
}
