package com.example.fixmyroad.network

import com.example.fixmyroad.Report
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("reports")
    suspend fun uploadReport(@Body report: Report): ReportResponse
}

data class ReportResponse(
    val success: Boolean,
    val message: String
)

object RetrofitClient {
    private const val BASE_URL = "https://example.com/api/" // Placeholder

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
