package com.wayfarer.app.ui.hotels

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.wayfarer.app.databinding.FragmentHotelsBinding
import com.wayfarer.app.utils.Resource

class HotelFragment : Fragment() {

    private var _binding: FragmentHotelsBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: HotelViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHotelsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[HotelViewModel::class.java]

        val adapter = HotelAdapter { hotel ->
            Toast.makeText(requireContext(), "Selected ${hotel.name}", Toast.LENGTH_SHORT).show()
        }

        binding.rvHotels.layoutManager = LinearLayoutManager(requireContext())
        binding.rvHotels.adapter = adapter

        viewModel.hotels.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> binding.progressBar.visibility = View.VISIBLE
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    adapter.submitList(resource.data)
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
