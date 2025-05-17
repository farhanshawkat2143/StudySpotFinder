package com.example.studyspotfinder.ui

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ImageButton
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studyspotfinder.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class CheckInFragment : Fragment(R.layout.fragment_check_in) {

    private lateinit var tvUserName: TextView
    private lateinit var tvUserEmail: TextView
    private lateinit var rvContributions: RecyclerView
    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()
    private lateinit var adapter: ContributionAdapter
    private val contributions = mutableListOf<String>() // Placeholder for spot names

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tvUserName = view.findViewById(R.id.tvUserName)
        tvUserEmail = view.findViewById(R.id.tvUserEmail)
        rvContributions = view.findViewById(R.id.rvContributions)
        val btnSettings: ImageButton = view.findViewById(R.id.btnSettings)
        btnSettings.setOnClickListener {
            startActivity(Intent(requireContext(), SettingsActivity::class.java))
        }

        val user = auth.currentUser
        if (user != null) {
            tvUserName.text = "Name: ${user.displayName ?: "Guest"}"
            tvUserEmail.text = "Email: ${user.email}"
        }

        adapter = ContributionAdapter(contributions)
        rvContributions.layoutManager = LinearLayoutManager(requireContext())
        rvContributions.adapter = adapter

        loadUserSpots()
    }

    private fun loadUserSpots() {
        val currentEmail = auth.currentUser?.email ?: return

        db.collection("spots")
            .whereEqualTo("addedBy", currentEmail)
            .get()
            .addOnSuccessListener { result ->
                contributions.clear()
                for (doc in result) {
                    val name = doc.getString("name")
                    if (!name.isNullOrEmpty()) {
                        contributions.add(name)
                    }
                }
                adapter.notifyDataSetChanged()
            }
            .addOnFailureListener {
                // Handle error
            }
    }
}
