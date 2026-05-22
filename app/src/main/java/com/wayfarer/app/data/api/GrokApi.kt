package com.wayfarer.app.data.api

import com.wayfarer.app.data.models.GroqRequest
import com.wayfarer.app.data.models.GroqResponse
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST

interface GroqApi {
    @POST("chat/completions")
    suspend fun chatCompletion(
        @Header("Authorization") auth: String,
        @Body request: GroqRequest
    ): Response<GroqResponse>
}
