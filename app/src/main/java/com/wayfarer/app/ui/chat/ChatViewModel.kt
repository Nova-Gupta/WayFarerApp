package com.wayfarer.app.ui.chat

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.wayfarer.app.BuildConfig
import com.wayfarer.app.data.api.RetrofitClient
import com.wayfarer.app.data.models.ChatMessage
import com.wayfarer.app.data.models.GeminiContent
import com.wayfarer.app.data.models.GeminiPart
import com.wayfarer.app.data.models.GeminiRequest
import kotlinx.coroutines.launch

class ChatViewModel : ViewModel() {

    private val apiKey = BuildConfig.GEMINI_API_KEY
    
    private val _messages = MutableLiveData<List<ChatMessage>>(listOf(
        ChatMessage("Hello! I'm your WayFarer AI assistant. How can I help you today?", false)
    ))
    val messages: LiveData<List<ChatMessage>> = _messages

    fun sendMessage(text: String) {
        val currentList = _messages.value.orEmpty().toMutableList()
        currentList.add(ChatMessage(text, true))
        _messages.value = currentList

        generateAIResponse(text)
    }

    private fun generateAIResponse(userText: String) {
        viewModelScope.launch {
            try {
                // System instructions
                val systemInstruction = "You are a helpful travel assistant for the WayFarer app. You help users find tours, hotels, and plan their trips. Keep responses concise and friendly."
                
                val conversation = mutableListOf<GeminiContent>()
                
                // Get history, skip the first greeting
                val history = _messages.value.orEmpty().drop(1)
                
                // Gemini expects perfectly alternating User -> Model roles starting with User
                history.forEachIndexed { index, chatMessage ->
                    val role = if (chatMessage.isUser) "user" else "model"
                    // Prepend system instruction to the very first user message in the list
                    val contentText = if (index == 0 && chatMessage.isUser) {
                        "System Instructions: $systemInstruction\n\nUser: ${chatMessage.text}"
                    } else {
                        chatMessage.text
                    }
                    
                    conversation.add(
                        GeminiContent(
                            role = role,
                            parts = listOf(GeminiPart(contentText))
                        )
                    )
                }

                // If conversation is empty (only the greeting was present), add the current message
                if (conversation.isEmpty()) {
                    conversation.add(
                        GeminiContent(
                            role = "user", 
                            parts = listOf(GeminiPart("System Instructions: $systemInstruction\n\nUser: $userText"))
                        )
                    )
                }

                val response = RetrofitClient.geminiApi.generateContent(
                    apiKey,
                    GeminiRequest(contents = conversation)
                )

                if (response.isSuccessful && response.body() != null) {
                    val aiText = response.body()?.candidates?.firstOrNull()?.content?.parts?.firstOrNull()?.text 
                        ?: "I'm sorry, I couldn't process that response."
                    addBotMessage(aiText)
                } else {
                    val errorMsg = response.errorBody()?.string() ?: ""
                    addBotMessage("Gemini Error ${response.code()}: $errorMsg")
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
