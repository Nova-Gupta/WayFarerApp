package com.wayfarer.app.ui.booking

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wayfarer.app.R
import com.wayfarer.app.data.models.Hotel
import com.wayfarer.app.databinding.ItemHotelBookingBinding

class HotelBookingAdapter(
    private val onSelect: (Hotel) -> Unit
) : ListAdapter<Hotel, HotelBookingAdapter.HotelVH>(DIFF) {

    private var selectedId: String = ""

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Hotel>() {
            override fun areItemsTheSame(a: Hotel, b: Hotel) = a.id == b.id
            override fun areContentsTheSame(a: Hotel, b: Hotel) = a == b
        }
    }

    fun setSelected(id: String) {
        val old = selectedId
        selectedId = id
        val list = currentList
        val oldIndex = list.indexOfFirst { it.id == old }
        val newIndex = list.indexOfFirst { it.id == id }
        if (oldIndex >= 0) notifyItemChanged(oldIndex)
        if (newIndex >= 0) notifyItemChanged(newIndex)
    }

    inner class HotelVH(private val binding: ItemHotelBookingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(hotel: Hotel) {
            binding.tvHotelName.text = hotel.name
            binding.tvHotelLocation.text = hotel.location
            binding.tvHotelRating.text = "★ ${hotel.rating}"
            binding.tvHotelPrice.text = "$${hotel.pricePerNight.toInt()}"

            val isSelected = hotel.id == selectedId
            binding.tvCheckSelected.visibility = if (isSelected) android.view.View.VISIBLE else android.view.View.GONE

            val strokeColor = if (isSelected)
                ContextCompat.getColor(binding.root.context, R.color.dark_navy)
            else
                ContextCompat.getColor(binding.root.context, R.color.divider)
            binding.cardHotel.strokeColor = strokeColor
            binding.cardHotel.strokeWidth = if (isSelected) 3 else 1

            Glide.with(binding.root.context)
                .load(hotel.image)
                .placeholder(R.drawable.bg_tour_placeholder)
                .centerCrop()
                .into(binding.ivHotelImage)

            binding.root.setOnClickListener { onSelect(hotel) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HotelVH {
        val binding = ItemHotelBookingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return HotelVH(binding)
    }

    override fun onBindViewHolder(holder: HotelVH, position: Int) {
        holder.bind(getItem(position))
    }
}
