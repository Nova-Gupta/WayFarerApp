package com.wayfarer.app.ui.detail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.wayfarer.app.databinding.BottomSheetWriteReviewBinding

class WriteReviewSheet : BottomSheetDialogFragment() {

    private var _binding: BottomSheetWriteReviewBinding? = null
    private val binding get() = _binding!!

    var onReviewSubmit: ((Int, String) -> Unit)? = null

    companion object {
        fun newInstance(tourName: String) = WriteReviewSheet().apply {
            arguments = Bundle().apply { putString("tourName", tourName) }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = BottomSheetWriteReviewBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.tvReviewTourName.text = arguments?.getString("tourName") ?: ""

        binding.ratingBar.setOnRatingBarChangeListener { _, rating, _ ->
            binding.tvRatingLabel.text = when (rating.toInt()) {
                1 -> "Poor"
                2 -> "Fair"
                3 -> "Good"
                4 -> "Very Good"
                5 -> "Excellent"
                else -> "Tap to rate"
            }
        }

        binding.btnSubmitReview.setOnClickListener {
            val rating = binding.ratingBar.rating.toInt()
            if (rating == 0) {
                Toast.makeText(requireContext(), "Please select a star rating", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            val comment = binding.etReviewComment.text?.toString()?.trim() ?: ""
            onReviewSubmit?.invoke(rating, comment)
            dismiss()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
