package com.wayfarer.app.ui.bookings

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.wayfarer.app.databinding.FragmentBookingsBinding
import com.wayfarer.app.ui.auth.AuthActivity
import com.wayfarer.app.utils.Resource

class BookingsFragment : Fragment() {

    private var _binding: FragmentBookingsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: BookingsViewModel
    private lateinit var adapter: BookingAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBookingsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (FirebaseAuth.getInstance().currentUser == null) {
            binding.tvEmpty.visibility = View.VISIBLE
            binding.rvBookings.visibility = View.GONE
            binding.progressBar.visibility = View.GONE
            binding.tvEmpty.setOnClickListener {
                startActivity(Intent(requireContext(), AuthActivity::class.java))
            }
            return
        }

        viewModel = ViewModelProvider(this)[BookingsViewModel::class.java]
        adapter = BookingAdapter { booking ->
            AlertDialog.Builder(requireContext())
                .setTitle("Cancel Booking")
                .setMessage("Cancel your booking for ${booking.tourName}? This cannot be undone.")
                .setPositiveButton("Cancel Booking") { _, _ -> viewModel.cancelBooking(booking.id) }
                .setNegativeButton("Keep It", null)
                .show()
        }

        binding.rvBookings.layoutManager = LinearLayoutManager(requireContext())
        binding.rvBookings.adapter = adapter

        viewModel.bookingsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    if (state.data.isEmpty()) {
                        binding.tvEmpty.visibility = View.VISIBLE
                        binding.rvBookings.visibility = View.GONE
                    } else {
                        binding.tvEmpty.visibility = View.GONE
                        binding.rvBookings.visibility = View.VISIBLE
                        adapter.submitList(state.data)
                    }
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.loadBookings()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
