package com.example.studyspotfinder.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.studyspotfinder.data.model.Spot
import com.example.studyspotfinder.databinding.ItemFavoriteSpotBinding

class FavoritesAdapter(
    private val spots: MutableList<Spot>,
    private val onItemClick: (Spot) -> Unit,
    private val onDeleteClick: (Spot, Int) -> Unit
) : RecyclerView.Adapter<FavoritesAdapter.FavoriteViewHolder>() {

    inner class FavoriteViewHolder(private val binding: ItemFavoriteSpotBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(spot: Spot) {
            binding.tvSpotName.text = spot.name
            binding.tvDescription.text = spot.description
            binding.root.setOnClickListener { onItemClick(spot) }
            binding.btnDelete.setOnClickListener { onDeleteClick(spot, adapterPosition) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FavoriteViewHolder {
        val binding = ItemFavoriteSpotBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return FavoriteViewHolder(binding)
    }

    override fun onBindViewHolder(holder: FavoriteViewHolder, position: Int) {
        holder.bind(spots[position])
    }

    override fun getItemCount(): Int = spots.size

    fun removeAt(position: Int) {
        spots.removeAt(position)
        notifyItemRemoved(position)
    }
}
