package com.wayfarer.app.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.wayfarer.app.R
import com.wayfarer.app.data.models.Tour

import com.wayfarer.app.databinding.FragmentHomeBinding
import com.wayfarer.app.utils.Resource

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: TourAdapter
    private var fullTourList: List<Tour> = emptyList()
    private var currentCategory = "All"

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        val displayName = FirebaseAuth.getInstance().currentUser?.displayName
        binding.tvGreeting.text = "Hello, ${displayName?.split(" ")?.firstOrNull() ?: "Traveller"} 👋"

        adapter = TourAdapter { tour ->
            val bundle = Bundle().apply {
                putString("tourId", tour.id)
                putString("tourName", tour.name)
                putString("tourSummary", tour.summary)
                putFloat("tourPrice", tour.price.toFloat())
                putString("tourCover", tour.imageCover)
                putString("tourDifficulty", tour.difficulty)
                putInt("tourDuration", tour.duration)
                putFloat("tourRating", tour.ratingsAverage)
                putInt("tourRatingCount", tour.ratingsQuantity)
                putStringArray("tourRoute", tour.route?.toTypedArray())
            }
            findNavController().navigate(R.id.action_home_to_detail, bundle)
        }

        binding.rvTours.layoutManager = LinearLayoutManager(requireContext())
        binding.rvTours.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener { viewModel.loadTours() }

        setupSearch()
        setupCategoryChips()

        viewModel.toursState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.swipeRefresh.isRefreshing = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    fullTourList = state.data
                    filterTours(binding.searchView.query.toString())
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.swipeRefresh.isRefreshing = false
                    Toast.makeText(requireContext(), state.message, Toast.LENGTH_LONG).show()
                }
            }
        }

        viewModel.loadTours()
    }

    private fun setupSearch() {
        binding.searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                filterTours(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterTours(newText)
                return true
            }
        })
    }

    private fun setupCategoryChips() {
        listOf(
            binding.chipCatAdventure, binding.chipCatNature,
            binding.chipCatCulture, binding.chipCatCity, binding.chipCatRelaxation
        ).forEach { it.setTextColor(androidx.core.content.ContextCompat.getColor(requireContext(), R.color.dark_navy)) }

        binding.chipGroupCategory.setOnCheckedStateChangeListener { _, checkedIds ->
            currentCategory = when (checkedIds.firstOrNull()) {
                R.id.chip_cat_adventure -> "Adventure"
                R.id.chip_cat_nature -> "Nature"
                R.id.chip_cat_culture -> "Culture"
                R.id.chip_cat_city -> "City"
                R.id.chip_cat_relaxation -> "Relaxation"
                else -> "All"
            }
            filterTours(binding.searchView.query.toString())
        }
    }

    private fun filterTours(query: String?) {
        var list = fullTourList
        if (currentCategory != "All") {
            list = list.filter { it.difficulty.equals(currentCategory, ignoreCase = true) }
        }
        if (!query.isNullOrBlank()) {
            list = list.filter {
                it.name.contains(query, ignoreCase = true) ||
                it.summary.contains(query, ignoreCase = true)
            }
        }
        adapter.submitList(list)
        binding.tvTourCount.text = "${list.size} tours found"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
