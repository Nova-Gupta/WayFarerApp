package com.wayfarer.app.ui.detail

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.wayfarer.app.R
import com.wayfarer.app.WayFarerApp
import com.wayfarer.app.databinding.FragmentTourDetailBinding
import com.wayfarer.app.ui.auth.AuthActivity
import com.wayfarer.app.ui.booking.BookingBottomSheet
import com.wayfarer.app.utils.NotificationHelper
import com.wayfarer.app.utils.Resource

class TourDetailFragment : Fragment() {

    private var _binding: FragmentTourDetailBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: DetailViewModel
    private lateinit var reviewAdapter: ReviewAdapter

    private var tourId = ""
    private var tourName = ""
    private var tourPrice = 0.0
    private var tourDuration = 0

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
            tourPrice = args.getFloat("tourPrice", 0f).toDouble()
            tourName = args.getString("tourName", "")
            tourDuration = args.getInt("tourDuration", 0)

            val summary = args.getString("tourSummary", "")
            val cover = args.getString("tourCover", "")
            val rating = args.getFloat("tourRating", 0f)
            val ratingCount = args.getInt("tourRatingCount", 0)
            val route = args.getStringArray("tourRoute")?.toList() ?: emptyList()

            binding.tvTourName.text = tourName
            binding.tvTourSummary.text = summary
            binding.tvTourPrice.text = "$${tourPrice.toInt()}"
            binding.tvTourDuration.text = "$tourDuration Days"
            binding.tvTourRating.text = "★ $rating ($ratingCount reviews)"

            setupRouteTimeline(route)

            Glide.with(requireContext())
                .load(cover)
                .placeholder(R.drawable.bg_tour_placeholder)
                .centerCrop()
                .into(binding.ivTourCover)
        }

        setupReviews()

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
            openBookingSheet()
        }

        viewModel.bookingState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnBook.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), "Booking confirmed!", Toast.LENGTH_LONG).show()
                    binding.btnBook.text = "Confirmed ✓"
                    binding.btnBook.isEnabled = false

                    val notificationHelper = NotificationHelper(requireContext())
                    notificationHelper.showBookingNotification(
                        "Booking Confirmed!",
                        "Your tour '${binding.tvTourName.text}' has been booked successfully."
                    )
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

        viewModel.submitReviewState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Success -> Toast.makeText(requireContext(), "Review submitted!", Toast.LENGTH_SHORT).show()
                is Resource.Error -> Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                else -> {}
            }
        }
    }

    private fun setupReviews() {
        reviewAdapter = ReviewAdapter()
        binding.rvReviews.layoutManager = LinearLayoutManager(requireContext())
        binding.rvReviews.adapter = reviewAdapter
        binding.rvReviews.isNestedScrollingEnabled = false

        binding.btnWriteReview.setOnClickListener {
            val session = (requireActivity().application as WayFarerApp).sessionManager
            if (!session.isLoggedIn()) {
                Toast.makeText(requireContext(), "Please sign in to leave a review", Toast.LENGTH_SHORT).show()
                startActivity(Intent(requireContext(), AuthActivity::class.java))
                return@setOnClickListener
            }
            val sheet = WriteReviewSheet.newInstance(tourName)
            sheet.onReviewSubmit = { rating, comment ->
                viewModel.submitReview(tourId, rating, comment)
            }
            sheet.show(parentFragmentManager, "write_review")
        }

        viewModel.reviewsState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.pbReviews.visibility = View.VISIBLE
                    binding.tvNoReviews.visibility = View.GONE
                }
                is Resource.Success -> {
                    binding.pbReviews.visibility = View.GONE
                    val reviews = state.data
                    if (reviews.isEmpty()) {
                        binding.tvNoReviews.visibility = View.VISIBLE
                        binding.rvReviews.visibility = View.GONE
                        binding.tvReviewsAverage.text = "—"
                        binding.tvReviewsStars.text = "☆☆☆☆☆"
                        binding.tvReviewsTotal.text = "No reviews yet"
                        binding.tvReviewCount.text = ""
                        binding.tvTourRating.text = "No reviews yet"
                    } else {
                        binding.tvNoReviews.visibility = View.GONE
                        binding.rvReviews.visibility = View.VISIBLE
                        reviewAdapter.submitList(reviews)

                        val avg = reviews.map { it.rating }.average()
                        val rounded = Math.round(avg * 10) / 10.0
                        val fullStars = avg.toInt()
                        binding.tvReviewsAverage.text = rounded.toString()
                        binding.tvReviewsStars.text = "★".repeat(fullStars) + "☆".repeat(5 - fullStars)
                        binding.tvReviewsTotal.text = "Based on ${reviews.size} review${if (reviews.size > 1) "s" else ""}"
                        binding.tvReviewCount.text = "${reviews.size} review${if (reviews.size > 1) "s" else ""}"
                        binding.tvTourRating.text = "★ $rounded (${reviews.size} review${if (reviews.size > 1) "s" else ""})"
                    }
                }
                is Resource.Error -> {
                    binding.pbReviews.visibility = View.GONE
                }
            }
        }

        if (tourId.isNotEmpty()) viewModel.loadReviews(tourId)
    }

    private fun openBookingSheet() {
        val sheet = BookingBottomSheet.newInstance(tourId, tourName, tourPrice, tourDuration)
        sheet.onBookingConfirmed = { request ->
            viewModel.bookTour(request)
        }
        sheet.show(parentFragmentManager, "booking_sheet")
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
