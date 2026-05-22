package com.wayfarer.app.data.models

import com.google.gson.annotations.SerializedName

// ── Auth / User ───────────────────────────────────────────────────────────

data class User(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val role: String = "user",
    val photo: String? = null
)

// ── Tours ─────────────────────────────────────────────────────────────────

data class Tour(
    val id: String,
    val name: String,
    val duration: Int,
    val maxGroupSizeString: String?,
    val difficulty: String,
    val ratingsAverage: Float = 0f,
    val ratingsQuantity: Int = 0,
    val price: Double,
    val summary: String,
    val image: String,
    val route: List<String>? = null
) {
    val imageCover: String get() = image
    val maxGroupSize: Int get() = maxGroupSizeString?.filter { it.isDigit() }?.toIntOrNull() ?: 20
}

// ── Bookings ──────────────────────────────────────────────────────────────

data class Booking(
    val id: String = "",
    val tourId: String = "",
    val tourName: String = "",
    val tourImage: String = "",
    val tourDifficulty: String = "",
    val tourDuration: Int = 0,
    val price: Double = 0.0,
    val checkInDate: Long = 0L,
    val checkOutDate: Long = 0L,
    val hotelId: String = "",
    val hotelName: String = "",
    val hotelPricePerNight: Double = 0.0,
    val nights: Int = 0,
    val guests: Int = 1,
    val tripType: String = "Solo",
    val paymentMethod: String = "Card",
    val totalAmount: Double = 0.0,
    val createdAt: Long = 0L,
    val paid: Boolean = true
)

data class BookingRequest(
    val tourId: String,
    val tourPrice: Double,
    val checkInDate: Long,
    val checkOutDate: Long,
    val hotelId: String,
    val hotelName: String,
    val hotelPricePerNight: Double,
    val nights: Int,
    val guests: Int,
    val tripType: String,
    val paymentMethod: String,
    val totalAmount: Double
)

// ── Reviews ───────────────────────────────────────────────────────────────

data class Review(
    val id: String = "",
    val userId: String = "",
    val userName: String = "",
    val rating: Int = 5,
    val comment: String = "",
    val createdAt: Long = 0L
)

// ── Hotels ────────────────────────────────────────────────────────────────

data class Hotel(
    val id: String,
    val name: String,
    val location: String,
    val rating: Float,
    val pricePerNight: Double,
    val image: String,
    val description: String
)

// ── Chat ──────────────────────────────────────────────────────────────────

data class ChatMessage(
    val text: String,
    val isUser: Boolean,
    val timestamp: Long = System.currentTimeMillis()
)

// ── Groq API ─────────────────────────────────────────────────────────────

data class GroqRequest(
    val model: String = "llama-3.3-70b-versatile",
    val messages: List<GroqMessage>
)

data class GroqMessage(
    val role: String,
    val content: String
)

data class GroqResponse(
    val id: String?,
    val choices: List<GroqChoice>?
)

data class GroqChoice(
    val message: GroqMessage?,
    @SerializedName("finish_reason") val finishReason: String?
)
