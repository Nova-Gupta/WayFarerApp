package com.wayfarer.app.data.api

import com.wayfarer.app.data.models.*
import retrofit2.Response
import retrofit2.http.*

interface WayFarerApi {

    // ── Auth ──────────────────────────────────────────────────────────────

    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("api/auth/register")
    suspend fun register(@Body request: RegisterRequest): Response<AuthResponse>

    @GET("api/auth/me")
    suspend fun getMe(): Response<AuthResponse>

    // ── Tours ──────────────────────────────────────────────────────────────

    @GET("api/tours")
    suspend fun getTours(): Response<List<Tour>>

    @GET("api/tours/{id}")
    suspend fun getTourById(@Path("id") id: String): Response<Tour>

    // ── Bookings ──────────────────────────────────────────────────────────

    @GET("api/bookings/my-tours")
    suspend fun getMyBookings(): Response<BookingsResponse>

    @POST("api/bookings")
    suspend fun createBooking(@Body request: BookingRequest): Response<BookingsResponse>

    // ── Hotels ──────────────────────────────────────────────────────────

    @GET("api/hotels")
    suspend fun getHotels(): Response<List<Hotel>>

    // ── Locations ────────────────────────────────────────────────────────

    @POST("api/locations")
    suspend fun addLocation(@Body location: Location): Response<Void>
}
