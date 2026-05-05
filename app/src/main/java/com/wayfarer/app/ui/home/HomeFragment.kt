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
import com.wayfarer.app.R
import com.wayfarer.app.WayFarerApp
import com.wayfarer.app.data.models.Tour
import com.wayfarer.app.databinding.FragmentHomeBinding
import com.wayfarer.app.utils.Resource

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HomeViewModel
    private lateinit var adapter: TourAdapter
    private var fullTourList: List<Tour> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[HomeViewModel::class.java]

        val user = (requireActivity().application as WayFarerApp).sessionManager.getUser()
        binding.tvGreeting.text = "Hello, ${user?.name?.split(" ")?.firstOrNull() ?: "Traveller"} 👋"

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

    private fun filterTours(query: String?) {
        val filteredList = if (query.isNullOrBlank()) {
            fullTourList
        } else {
            fullTourList.filter { 
                it.name.contains(query, ignoreCase = true) || 
                it.summary.contains(query, ignoreCase = true) 
            }
        }
        adapter.submitList(filteredList)
        binding.tvTourCount.text = "${filteredList.size} tours found"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
