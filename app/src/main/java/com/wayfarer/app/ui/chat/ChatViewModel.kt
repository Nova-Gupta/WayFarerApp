package com.wayfarer.app.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wayfarer.app.BuildConfig
import com.wayfarer.app.data.api.RetrofitClient
import com.wayfarer.app.data.models.ChatMessage
import com.wayfarer.app.data.models.GroqMessage
import com.wayfarer.app.data.models.GroqRequest
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val _messages = MutableLiveData<List<ChatMessage>>(listOf(
        ChatMessage("Hello! I'm your WayFarer AI assistant. How can I help you plan your next trip?", false)
    ))
    val messages: LiveData<List<ChatMessage>> = _messages

    fun sendMessage(text: String) {
        val currentList = _messages.value.orEmpty().toMutableList()
        currentList.add(ChatMessage(text, true))
        _messages.value = currentList
        generateAIResponse()
    }

    private fun generateAIResponse() {
        viewModelScope.launch {
            try {
                val systemMessage = GroqMessage(
                    role = "system",
                    content = "You are a helpful travel assistant for the WayFarer app. Help users discover tours, hotels, and plan their trips. Keep responses concise and friendly."
                )

                val historyMessages = _messages.value.orEmpty()
                    .drop(1) // skip the initial greeting
                    .map { msg ->
                        GroqMessage(
                            role = if (msg.isUser) "user" else "assistant",
                            content = msg.text
                        )
                    }

                val request = GroqRequest(
                    model = "llama-3.3-70b-versatile",
                    messages = listOf(systemMessage) + historyMessages
                )

                val response = RetrofitClient.groqApi.chatCompletion(
                    auth = "Bearer ${BuildConfig.GROK_API_KEY}",
                    request = request
                )

                if (response.isSuccessful && response.body() != null) {
                    val text = response.body()?.choices?.firstOrNull()?.message?.content
                        ?: "Sorry, I couldn't generate a response."
                    addBotMessage(text)
                } else {
                    addBotMessage("Error ${response.code()}: ${response.errorBody()?.string()?.take(200)}")
                }
            } catch (e: Exception) {
                addBotMessage("Connection error: ${e.localizedMessage}")
            }
        }
    }

    private fun addBotMessage(text: String) {
        val currentList = _messages.value.orEmpty().toMutableList()
        currentList.add(ChatMessage(text, false))
        _messages.value = currentList
    }
}
