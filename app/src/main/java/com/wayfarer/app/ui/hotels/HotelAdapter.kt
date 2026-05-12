package com.wayfarer.app.ui.hotels

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wayfarer.app.R
import com.wayfarer.app.data.models.Hotel
import com.wayfarer.app.databinding.ItemHotelBinding

class HotelAdapter(private val onHotelClick: (Hotel) -> Unit) :
    ListAdapter<Hotel, HotelAdapter.HotelViewHolder>(HotelDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotelViewHolder {
        val binding = ItemHotelBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HotelViewHolder(binding)
    }

    override fun onBindViewHolder(holder: HotelViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class HotelViewHolder(private val binding: ItemHotelBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(hotel: Hotel) {
            binding.tvHotelName.text = hotel.name
            binding.tvHotelLocation.text = hotel.location
            binding.tvHotelRating.text = "★ ${hotel.rating}"
            binding.tvHotelPrice.text = "$${hotel.pricePerNight.toInt()}/night"

            Glide.with(binding.ivHotel.context)
                .load(hotel.image)
                .placeholder(R.drawable.bg_tour_placeholder)
                .centerCrop()
                .into(binding.ivHotel)

            binding.root.setOnClickListener { onHotelClick(hotel) }
        }
    }

    class HotelDiffCallback : DiffUtil.ItemCallback<Hotel>() {
        override fun areItemsTheSame(oldItem: Hotel, newItem: Hotel): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Hotel, newItem: Hotel): Boolean = oldItem == newItem
    }
}
