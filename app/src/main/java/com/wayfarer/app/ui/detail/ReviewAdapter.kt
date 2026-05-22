package com.wayfarer.app.ui.detail

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wayfarer.app.data.models.Review
import com.wayfarer.app.databinding.ItemReviewBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReviewAdapter : ListAdapter<Review, ReviewAdapter.ReviewViewHolder>(DIFF) {

    companion object {
        val DIFF = object : DiffUtil.ItemCallback<Review>() {
            override fun areItemsTheSame(a: Review, b: Review) = a.id == b.id
            override fun areContentsTheSame(a: Review, b: Review) = a == b
        }
        private val DATE_FMT = SimpleDateFormat("MMM yyyy", Locale.getDefault())
    }

    inner class ReviewViewHolder(private val binding: ItemReviewBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(review: Review) {
            val initials = review.userName
                .split(" ")
                .mapNotNull { it.firstOrNull()?.toString() }
                .take(2).joinToString("").uppercase().ifEmpty { "?" }

            binding.tvReviewerInitials.text = initials
            binding.tvReviewerName.text = review.userName
            binding.tvReviewDate.text = DATE_FMT.format(Date(review.createdAt))
            binding.tvReviewStars.text = "★".repeat(review.rating) + "☆".repeat(5 - review.rating)
            binding.tvReviewComment.text = review.comment
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReviewViewHolder {
        val binding = ItemReviewBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ReviewViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ReviewViewHolder, position: Int) {
        holder.bind(getItem(position))
    }
}
