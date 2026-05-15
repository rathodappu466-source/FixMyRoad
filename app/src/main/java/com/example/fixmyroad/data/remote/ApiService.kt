package com.example.fixmyroad.data.remote

import com.example.fixmyroad.domain.model.Report
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
