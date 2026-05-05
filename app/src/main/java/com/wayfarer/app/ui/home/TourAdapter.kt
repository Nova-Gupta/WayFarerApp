package com.wayfarer.app.ui.home

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wayfarer.app.R
import com.wayfarer.app.data.api.RetrofitClient
import com.wayfarer.app.data.models.Tour
import com.wayfarer.app.databinding.ItemTourBinding

class TourAdapter(private val onClick: (Tour) -> Unit) :
    ListAdapter<Tour, TourAdapter.TourViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Tour>() {
            override fun areItemsTheSame(a: Tour, b: Tour) = a.id == b.id
            override fun areContentsTheSame(a: Tour, b: Tour) = a == b
        }
    }

    inner class TourViewHolder(private val binding: ItemTourBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(tour: Tour) {
            val context = binding.root.context
            binding.tvTourName.text = tour.name
            binding.tvTourSummary.text = tour.summary

            // Use resource strings with placeholders to resolve lint warnings
            binding.tvTourPrice.text = context.getString(R.string.tour_price_format, tour.price.toInt())
            binding.tvTourDuration.text = context.getString(R.string.tour_duration_format, tour.duration)
            binding.tvTourDifficulty.text = tour.difficulty.replaceFirstChar { it.uppercase() }
            binding.tvTourRating.text = context.getString(R.string.tour_rating_format, tour.ratingsAverage)

            // Check if the image string is a full URL or just a filename
            val imageUrl = if (tour.imageCover.startsWith("http")) {
                tour.imageCover
            } else {
                "${RetrofitClient.BASE_URL}img/tours/${tour.imageCover}"
            }

            Glide.with(context)
                .load(imageUrl)
                .placeholder(R.drawable.bg_tour_placeholder)
                .centerCrop()
                .into(binding.ivTourCover)

            binding.root.setOnClickListener { onClick(tour) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TourViewHolder {
        val binding = ItemTourBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TourViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TourViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
