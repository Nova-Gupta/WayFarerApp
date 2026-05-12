package com.wayfarer.app.data.models

import com.google.gson.annotations.SerializedName

// ── Auth ──────────────────────────────────────────────────────────────────

data class LoginRequest(
    val email: String,
    val password: String
)

data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String,
    @SerializedName("passwordConfirm") val passwordConfirm: String
)

data class AuthResponse(
    @SerializedName("_id") val id: String?,
    val name: String?,
    val email: String?,
    val role: String?,
    val token: String?,
    val message: String? // For error cases
)

data class User(
    @SerializedName("_id") val id: String,
    val name: String,
    val email: String,
    val role: String = "user",
    val photo: String? = null
)

// ── Tours ─────────────────────────────────────────────────────────────────

data class ToursResponse(
    val status: String,
    val results: Int,
    val data: ToursData?
)

data class ToursData(
    val tours: List<Tour>?
)

data class Tour(
    @SerializedName("_id") val mongoId: String?,
    val id: String,
    @SerializedName("title") val name: String,
    val duration: Int,
    @SerializedName("groupSize") val maxGroupSizeString: String?,
    @SerializedName("category") val difficulty: String,
    @SerializedName("rating") val ratingsAverage: Float = 0f,
    val ratingsQuantity: Int = 0,
    val price: Double,
    @SerializedName("description") val summary: String,
    val image: String,
    val route: List<String>? = null
) {
    val imageCover: String get() = image
    val maxGroupSize: Int get() = maxGroupSizeString?.filter { it.isDigit() }?.toIntOrNull() ?: 20
}

data class Location(
    val description: String,
    val type: String = "Point",
    val coordinates: List<Double>,
    val category: String = "Other",
    val recommendedBy: String? = null,
    val whyVisit: String? = null
)

// ── Bookings ──────────────────────────────────────────────────────────────

data class BookingRequest(
    val tour: String,
    val price: Double
)

data class BookingsResponse(
    val status: String,
    val results: Int?,
    val data: BookingsData?
)

data class BookingsData(
    val bookings: List<Booking>?
)

data class Booking(
    @SerializedName("_id") val id: String,
    val tour: Tour?,
    val user: User?,
    val price: Double,
    val createdAt: String,
    val paid: Boolean = true
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

// ── Generic ───────────────────────────────────────────────────────────────

data class ApiError(
    val status: String,
    val message: String
)
