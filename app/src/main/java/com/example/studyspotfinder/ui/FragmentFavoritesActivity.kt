package com.example.studyspotfinder.ui

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studyspotfinder.data.model.Spot
import com.example.studyspotfinder.databinding.FragmentFavoritesBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldPath
import com.google.firebase.firestore.FirebaseFirestore

class FavoritesFragment : Fragment() {

    private var _binding: FragmentFavoritesBinding? = null
    private val binding get() = _binding!!

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val favoriteSpots = mutableListOf<Spot>()
    private lateinit var adapter: FavoritesAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFavoritesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        adapter = FavoritesAdapter(favoriteSpots,
            onItemClick = { spot ->
                val intent = Intent(requireContext(), SpotDetailsActivity::class.java)
                intent.putExtra("spotId", spot.id)
                startActivity(intent)
            },
            onDeleteClick = { spot, position ->
                deleteFavorite(spot, position)
            }
        )

        binding.rvFavorites.layoutManager = LinearLayoutManager(requireContext())
        binding.rvFavorites.adapter = adapter

        loadFavorites()
    }

    private fun loadFavorites() {
        val user = auth.currentUser
        if (user == null) {
            Toast.makeText(requireContext(), "Please login to see favorites", Toast.LENGTH_SHORT).show()
            return
        }

        firestore.collection("users").document(user.uid).collection("favorites").get()
            .addOnSuccessListener { docs ->
                favoriteSpots.clear()
                if (docs.isEmpty) {
                    adapter.notifyDataSetChanged()
                    return@addOnSuccessListener
                }

                val spotIds = docs.map { it.id }

                firestore.collection("spots")
                    .whereIn(FieldPath.documentId(), spotIds)
                    .get()
                    .addOnSuccessListener { spotsDocs ->
                        for (doc in spotsDocs) {
                            val spot = doc.toObject(Spot::class.java).copy(id = doc.id)
                            favoriteSpots.add(spot)
                        }
                        adapter.notifyDataSetChanged()
                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(), "Error loading spots: ${it.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error loading favorites: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun deleteFavorite(spot: Spot, position: Int) {
        val user = auth.currentUser ?: return

        firestore.collection("users")
            .document(user.uid)
            .collection("favorites")
            .document(spot.id)
            .delete()
            .addOnSuccessListener {
                adapter.removeAt(position)
                Toast.makeText(requireContext(), "Removed from favorites", Toast.LENGTH_SHORT).show()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error removing: ${it.message}", Toast.LENGTH_SHORT).show()
            }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
