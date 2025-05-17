package com.example.studyspotfinder.ui

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.example.studyspotfinder.R
import com.example.studyspotfinder.databinding.ActivitySplashBinding

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load animations
        val logoAnim = AnimationUtils.loadAnimation(this, R.anim.logo_anim)
        val taglineAnim = AnimationUtils.loadAnimation(this, R.anim.tagline_anim)

        // Start animations
        binding.logoImage.startAnimation(logoAnim)
        binding.taglineText.startAnimation(taglineAnim)

        // Delay to next activity
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 2500)
    }
}
