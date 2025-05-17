package com.example.studyspotfinder.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studyspotfinder.R

class ContributionAdapter(private val items: List<String>) :
    RecyclerView.Adapter<ContributionAdapter.ContributionViewHolder>() {

    inner class ContributionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvSpotName: TextView = view.findViewById(R.id.tvSpotName)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContributionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_contribution, parent, false)
        return ContributionViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContributionViewHolder, position: Int) {
        holder.tvSpotName.text = items[position]
    }

    override fun getItemCount(): Int = items.size
}
