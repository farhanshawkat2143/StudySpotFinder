package com.example.studyspotfinder.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.studyspotfinder.R
import com.example.studyspotfinder.databinding.ActivityMapBinding

class MapActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load HomeFragment initially
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, HomeFragment())
            .commit()

        binding.bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, HomeFragment())
                        .commit()
                    true
                }
                R.id.nav_favorites -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, FavoritesFragment())
                        .commit()
                    true
                }
                R.id.nav_checkin -> {
                    supportFragmentManager.beginTransaction()
                        .replace(R.id.fragmentContainer, CheckInFragment())
                        .commit()
                    true
                }
                else -> false
            }
        }
    }
}
