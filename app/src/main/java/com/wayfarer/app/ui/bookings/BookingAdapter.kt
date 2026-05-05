package com.wayfarer.app.ui.bookings

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wayfarer.app.R
import com.wayfarer.app.data.api.RetrofitClient
import com.wayfarer.app.data.models.Booking
import com.wayfarer.app.databinding.ItemBookingBinding

class BookingAdapter : ListAdapter<Booking, BookingAdapter.BookingViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Booking>() {
            override fun areItemsTheSame(a: Booking, b: Booking) = a.id == b.id
            override fun areContentsTheSame(a: Booking, b: Booking) = a == b
        }
    }

    inner class BookingViewHolder(private val binding: ItemBookingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: Booking) {
            binding.tvTourName.text = booking.tour?.name ?: "Tour"
            binding.tvPrice.text = "Paid: $${booking.price.toInt()}"
            binding.tvDate.text = "Booked: ${booking.createdAt.take(10)}"
            binding.tvStatus.text = if (booking.paid) "Confirmed ✓" else "Pending"

            val cover = booking.tour?.imageCover ?: ""
            if (cover.isNotEmpty()) {
                val url = "${RetrofitClient.BASE_URL}img/tours/$cover"
                Glide.with(binding.root.context)
                    .load(url)
                    .placeholder(R.drawable.bg_tour_placeholder)
                    .centerCrop()
                    .into(binding.ivTourThumb)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BookingViewHolder {
        val binding = ItemBookingBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return BookingViewHolder(binding)
    }

    override fun onBindViewHolder(holder: BookingViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
