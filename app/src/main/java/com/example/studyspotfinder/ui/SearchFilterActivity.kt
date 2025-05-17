package com.example.studyspotfinder.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.SeekBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.studyspotfinder.databinding.ActivitySearchFilterBinding

class SearchFilterActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySearchFilterBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySearchFilterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Initialize labels with current descriptive text
        binding.labelWifiSpeed.text = mapWifiSpeedLevel(binding.seekBarWifiSpeed.progress)
        binding.labelNoiseLevel.text = mapNoiseLevel(binding.seekBarNoiseLevel.progress)
        binding.labelSeating.text = mapSeatingLevel(binding.seekBarSeating.progress)
        binding.labelPowerOutlets.text = if (binding.seekBarPowerOutlets.progress >= 50) "On" else "Off"

        // WiFi Speed SeekBar listener
        binding.seekBarWifiSpeed.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.labelWifiSpeed.text = mapWifiSpeedLevel(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Noise Level SeekBar listener
        binding.seekBarNoiseLevel.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.labelNoiseLevel.text = mapNoiseLevel(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Seating Capacity SeekBar listener
        binding.seekBarSeating.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.labelSeating.text = mapSeatingLevel(progress)
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Power Outlets SeekBar listener
        binding.seekBarPowerOutlets.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.labelPowerOutlets.text = if (progress >= 50) "On" else "Off"
            }
            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Apply Filters button
        binding.btnApplyFilters.setOnClickListener {
            val searchText = binding.etSearch.text.toString().trim()

            val wifiSpeed = mapWifiSpeedLevel(binding.seekBarWifiSpeed.progress)
            val noiseLevel = mapNoiseLevel(binding.seekBarNoiseLevel.progress)
            val seating = mapSeatingLevel(binding.seekBarSeating.progress)
            val powerOutlets = binding.seekBarPowerOutlets.progress >= 50

            val resultIntent = Intent().apply {
                putExtra("searchText", searchText)
                putExtra("wifiSpeed", wifiSpeed)
                putExtra("noiseLevel", noiseLevel)
                putExtra("seating", seating)
                putExtra("powerOutlets", powerOutlets)
            }

            setResult(Activity.RESULT_OK, resultIntent)
            Toast.makeText(this, "Filters applied", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun mapWifiSpeedLevel(progress: Int): String = when {
        progress >= 66 -> "High"
        progress >= 33 -> "Medium"
        else -> "Low"
    }

    private fun mapNoiseLevel(progress: Int): String = when {
        progress >= 66 -> "Noisy"
        progress >= 33 -> "Moderate"
        else -> "Quiet"
    }

    private fun mapSeatingLevel(progress: Int): String = when {
        progress >= 66 -> "Plenty"
        progress >= 33 -> "Moderate"
        else -> "Few"
    }
}
