package com.wayfarer.app.ui.chat

import android.view.Gravity
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.wayfarer.app.R
import com.wayfarer.app.data.models.ChatMessage
import com.wayfarer.app.databinding.ItemChatMessageBinding

class ChatAdapter : ListAdapter<ChatMessage, ChatAdapter.ChatViewHolder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val binding = ItemChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ChatViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class ChatViewHolder(private val binding: ItemChatMessageBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(message: ChatMessage) {
            binding.tvMessage.text = message.text
            
            val params = binding.cardMessage.layoutParams as FrameLayout.LayoutParams
            if (message.isUser) {
                params.gravity = Gravity.END
                binding.cardMessage.setCardBackgroundColor(binding.root.context.getColor(R.color.primary))
                binding.tvMessage.setTextColor(binding.root.context.getColor(android.R.color.white))
            } else {
                params.gravity = Gravity.START
                binding.cardMessage.setCardBackgroundColor(binding.root.context.getColor(R.color.surface))
                binding.tvMessage.setTextColor(binding.root.context.getColor(R.color.text_primary))
            }
            binding.cardMessage.layoutParams = params
        }
    }

    object DiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean = 
            oldItem.timestamp == newItem.timestamp && oldItem.text == newItem.text
        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean = oldItem == newItem
    }
}
