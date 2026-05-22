package com.wayfarer.app.ui.bookings

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.wayfarer.app.R
import com.wayfarer.app.data.models.Booking
import com.wayfarer.app.databinding.ItemBookingBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class BookingAdapter(private val onCancelClick: (Booking) -> Unit) : ListAdapter<Booking, BookingAdapter.BookingViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Booking>() {
            override fun areItemsTheSame(a: Booking, b: Booking) = a.id == b.id
            override fun areContentsTheSame(a: Booking, b: Booking) = a == b
        }
        private val BOOKED_FMT = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    }

    inner class BookingViewHolder(private val binding: ItemBookingBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(booking: Booking) {
            binding.tvTourName.text = booking.tourName.ifEmpty { "Tour" }
            binding.tvDate.text = "Booked: ${BOOKED_FMT.format(Date(booking.createdAt))}"
            binding.tvStatus.text = if (booking.paid) "Confirmed ✓" else "Pending"

            // Travel dates
            if (booking.checkInDate > 0L && booking.checkOutDate > 0L) {
                val checkIn = BOOKED_FMT.format(Date(booking.checkInDate))
                val checkOut = BOOKED_FMT.format(Date(booking.checkOutDate))
                binding.tvTravelDates.text = "📅  $checkIn → $checkOut  (${booking.nights} nights)"
                binding.tvTravelDates.visibility = View.VISIBLE
            } else {
                binding.tvTravelDates.visibility = View.GONE
            }

            // Hotel info
            if (booking.hotelName.isNotEmpty()) {
                binding.tvHotelInfo.text = "🏨  ${booking.hotelName}"
                binding.tvHotelInfo.visibility = View.VISIBLE
            } else {
                binding.tvHotelInfo.visibility = View.GONE
            }

            // Guests + trip type
            if (booking.guests > 0) {
                val guestLabel = "${booking.guests} guest${if (booking.guests > 1) "s" else ""}"
                binding.tvGuestsInfo.text = "👥  $guestLabel  •  ${booking.tripType}"
                binding.tvGuestsInfo.visibility = View.VISIBLE
            } else {
                binding.tvGuestsInfo.visibility = View.GONE
            }

            // Total amount (fall back to tour price for legacy bookings)
            val displayAmount = if (booking.totalAmount > 0) booking.totalAmount else booking.price
            binding.tvPrice.text = "Total: $${displayAmount.toInt()}"

            // Payment method
            if (booking.paymentMethod.isNotEmpty()) {
                binding.tvPaymentMethod.text = "💳 ${booking.paymentMethod}"
                binding.tvPaymentMethod.visibility = View.VISIBLE
            } else {
                binding.tvPaymentMethod.visibility = View.GONE
            }

            if (booking.tourImage.isNotEmpty()) {
                Glide.with(binding.root.context)
                    .load(booking.tourImage)
                    .placeholder(R.drawable.bg_tour_placeholder)
                    .centerCrop()
                    .into(binding.ivTourThumb)
            }

            binding.btnCancelBooking.setOnClickListener { onCancelClick(booking) }
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
