package com.example.fixmyroad.domain.repository

import android.net.Uri
import com.example.fixmyroad.domain.model.Report
import kotlinx.coroutines.flow.Flow

interface ReportRepository {
    fun getAllReports(): Flow<List<Report>>
    fun getReportById(ticketId: String): Flow<Report?>
    suspend fun submitReport(report: Report, localImageUri: Uri?)
    suspend fun updateReport(report: Report, imageUri: Uri? = null)
    suspend fun deleteReport(ticketId: String)
    suspend fun syncReportsFromFirebase()
    fun observeRemoteReports(): Flow<List<Report>>
}
