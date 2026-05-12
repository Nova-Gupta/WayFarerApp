package com.wayfarer.app.ui.location

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.wayfarer.app.databinding.FragmentAddLocationBinding
import com.wayfarer.app.utils.Resource

class AddLocationFragment : Fragment() {

    private var _binding: FragmentAddLocationBinding? = null
    private val binding get() = _binding!!
    private lateinit var viewModel: LocationViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddLocationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = ViewModelProvider(this)[LocationViewModel::class.java]

        setupCategoryDropdown()

        binding.btnAddLocation.setOnClickListener {
            val desc = binding.etDescription.text.toString()
            val category = binding.actvCategory.text.toString()
            val whyVisit = binding.etWhyVisit.text.toString()
            val lat = binding.etLat.text.toString().toDoubleOrNull() ?: 0.0
            val lng = binding.etLng.text.toString().toDoubleOrNull() ?: 0.0

            if (desc.isEmpty() || category.isEmpty() || whyVisit.isEmpty()) {
                Toast.makeText(requireContext(), "Please fill in the required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.addLocation(desc, lat, lng, category, whyVisit)
        }

        viewModel.addLocationState.observe(viewLifecycleOwner) { resource ->
            when (resource) {
                is Resource.Loading -> {
                    binding.progressBar.visibility = View.VISIBLE
                    binding.btnAddLocation.isEnabled = false
                }
                is Resource.Success -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnAddLocation.isEnabled = true
                    Toast.makeText(requireContext(), "Thank you for your recommendation!", Toast.LENGTH_SHORT).show()
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
                is Resource.Error -> {
                    binding.progressBar.visibility = View.GONE
                    binding.btnAddLocation.isEnabled = true
                    Toast.makeText(requireContext(), resource.message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun setupCategoryDropdown() {
        val categories = arrayOf("Viewpoint", "Restaurant", "Hidden Gem", "Historical", "Hiking", "Other")
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, categories)
        binding.actvCategory.setAdapter(adapter)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
