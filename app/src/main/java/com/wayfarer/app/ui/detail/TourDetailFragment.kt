package com.wayfarer.app.ui.detail

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.wayfarer.app.R
import com.wayfarer.app.WayFarerApp
import com.wayfarer.app.data.api.RetrofitClient
import com.wayfarer.app.databinding.FragmentTourDetailBinding
import com.wayfarer.app.ui.auth.AuthActivity
import com.wayfarer.app.utils.NotificationHelper
import com.wayfarer.app.utils.Resource

class TourDetailFragment : Fragment() {

    private var _binding: FragmentTourDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DetailViewModel

    private var tourId = ""
    private var tourPrice = 0.0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTourDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[DetailViewModel::class.java]

        arguments?.let { args ->
            tourId = args.getString("tourId", "")
            // Fixed: Use getFloat to match the NavGraph argument type and avoid crash
            tourPrice = args.getFloat("tourPrice", 0f).toDouble()
            val name = args.getString("tourName", "")
            val summary = args.getString("tourSummary", "")
            val cover = args.getString("tourCover", "")
            val rating = args.getFloat("tourRating", 0f)
            val ratingCount = args.getInt("tourRatingCount", 0)
            val duration = args.getInt("tourDuration", 0)
            // Fixed: Use getStringArray to match what HomeFragment is passing
            val route = args.getStringArray("tourRoute")?.toList() ?: emptyList()

            binding.tvTourName.text = name
            binding.tvTourSummary.text = summary
            binding.tvTourPrice.text = "$${tourPrice.toInt()}"
            binding.tvTourDuration.text = "$duration Days"
            binding.tvTourRating.text = "★ $rating ($ratingCount reviews)"

            setupRouteTimeline(route)

            // Fixed: Handle external URLs. If it starts with http, load directly.
            val imageUrl = if (cover?.startsWith("http") == true) {
                cover
            } else {
                "${RetrofitClient.BASE_URL}img/tours/$cover"
            }

            Glide.with(requireContext())
                .load(imageUrl)
                .placeholder(R.drawable.bg_tour_placeholder)
                .centerCrop()
                .into(binding.ivTourCover)
        }

        binding.btnBack.setOnClickListener {
            requireActivity().onBackPressedDispatcher.onBackPressed()
        }

        binding.btnBook.setOnClickListener {
            val session = (requireActivity().application as WayFarerApp).sessionManager
            if (!session.isLoggedIn()) {
                Toast.makeText(requireContext(), "Please sign in to book", Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), AuthActivity::class.java))
                return@setOnClickListener
            }
            if (tourId.isNotEmpty()) {
                viewModel.bookTour(tourId, tourPrice)
            }
        }

        viewModel.bookingState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnBook.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "🎉 Booking confirmed!", Toast.LENGTH_LONG).show()
                    binding.btnBook.text = "Confirmed ✓"
                    binding.btnBook.isEnabled = false

                    // Schedule notification
                    val notificationHelper = NotificationHelper(requireContext())
                    notificationHelper.showBookingNotification(
                        "Booking Confirmed!",
                        "Your tour '${binding.tvTourName.text}' has been booked successfully."
                    )
                    // Schedule a reminder for 30 seconds later for demonstration
                    notificationHelper.scheduleReminder(
                        "Tour Reminder",
                        "Get ready! Your tour '${binding.tvTourName.text}' is starting soon.",
                        30
                    )
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnBook.isEnabled = true
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    private fun setupRouteTimeline(route: List<String>) {
        binding.layoutRoute.removeAllViews()
        if (route.isEmpty()) {
            val tv = TextView(requireContext()).apply { 
                text = "No route data available"
                setTextColor(Color.GRAY)
            }
            binding.layoutRoute.addView(tv)
            return
        }

        route.forEachIndexed { index, city ->
            val cityView = TextView(requireContext()).apply {
                text = city
                setBackgroundResource(R.drawable.bg_info_chip)
                setPadding(32, 16, 32, 16)
                setTextColor(requireContext().getColor(R.color.text_primary))
                textSize = 13f
            }
            binding.layoutRoute.addView(cityView)

            if (index < route.size - 1) {
                val arrow = TextView(requireContext()).apply {
                    text = " → "
                    textSize = 18f
                    setPadding(16, 0, 16, 0)
                    gravity = Gravity.CENTER
                }
                binding.layoutRoute.addView(arrow)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
