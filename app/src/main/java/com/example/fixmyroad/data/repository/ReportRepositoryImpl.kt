package com.example.fixmyroad.data.repository

import android.net.Uri
import com.example.fixmyroad.data.local.ReportDao
import com.example.fixmyroad.data.local.entity.ReportEntity
import com.example.fixmyroad.domain.model.Report
import com.example.fixmyroad.domain.repository.ReportRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ReportRepositoryImpl @Inject constructor(
    private val reportDao: ReportDao,
    private val firebaseRepository: FirebaseRepository,
    private val storageRepository: FirebaseStorageRepository
) : ReportRepository {

    override fun getAllReports(): Flow<List<Report>> {
        return reportDao.getAllReports().map { entities ->
            entities.map { it.toDomain() }
        }.distinctUntilChanged()
    }

    override fun getReportById(ticketId: String): Flow<Report?> {
        return reportDao.getReportById(ticketId).map { entity ->
            entity?.toDomain()
        }.distinctUntilChanged()
    }

    override suspend fun submitReport(report: Report, localImageUri: Uri?) = withContext(Dispatchers.IO) {
        // 1. Initial local save (Optimistic UI state)
        // Store the local URI initially so the user sees the image immediately
        val initialReport = report.copy(imageUri = localImageUri?.toString())
        reportDao.insertReport(ReportEntity.fromDomain(initialReport))
        
        var finalReport = initialReport

        // 2. Handle Image Evidence Upload
        localImageUri?.let { uri ->
            try {
                // Compress and Upload to Storage
                val uploadResult = storageRepository.uploadReportImage(uri)
                if (uploadResult.isSuccess) {
                    val downloadUrl = uploadResult.getOrNull()
                    finalReport = finalReport.copy(imageUri = downloadUrl)
                    
                    // Update local DB with the permanent cloud URL
                    reportDao.updateReport(ReportEntity.fromDomain(finalReport))
                }
            } catch (e: Exception) {
                // Log failure but continue with report submission (allows offline-first sync later)
                e.printStackTrace()
            }
        }
        
        // 3. Push complete report to Firestore
        val firestoreResult = firebaseRepository.submitReport(finalReport)
        if (firestoreResult.isSuccess) {
            // Mark as synced in local DB
            reportDao.updateReport(ReportEntity.fromDomain(finalReport).copy(isSynced = true))
        } else {
            // Throw exception to be handled by ViewModel/UI if critical
            throw firestoreResult.exceptionOrNull() ?: Exception("Failed to sync with cloud")
        }
    }

    override suspend fun updateReport(report: Report, imageUri: Uri?) = withContext(Dispatchers.IO) {
        // 1. Update local cache optimistically
        reportDao.updateReport(ReportEntity.fromDomain(report))
        
        var updatedReport = report

        // 2. Handle new image upload if provided
        imageUri?.let { uri ->
            try {
                val uploadResult = storageRepository.uploadReportImage(uri)
                if (uploadResult.isSuccess) {
                    val downloadUrl = uploadResult.getOrNull()
                    updatedReport = updatedReport.copy(imageUri = downloadUrl)
                    
                    // Update local DB with new image URL
                    reportDao.updateReport(ReportEntity.fromDomain(updatedReport))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // 3. Sync to Firestore
        val firestoreResult = firebaseRepository.updateReport(updatedReport)
        if (firestoreResult.isSuccess) {
            // Mark as synced
            reportDao.updateReport(ReportEntity.fromDomain(updatedReport).copy(isSynced = true))
        } else {
            throw firestoreResult.exceptionOrNull() ?: Exception("Failed to update report")
        }
    }

    override suspend fun deleteReport(ticketId: String) = withContext(Dispatchers.IO) {
        // 1. Delete from local DB first
        reportDao.deleteReportById(ticketId)
        
        // 2. Delete from Firestore
        val firestoreResult = firebaseRepository.deleteReport(ticketId)
        if (firestoreResult.isFailure) {
            throw firestoreResult.exceptionOrNull() ?: Exception("Failed to delete report")
        }
    }

    override suspend fun syncReportsFromFirebase() = withContext(Dispatchers.IO) {
        val result = firebaseRepository.fetchReportsOnce()
        if (result.isSuccess) {
            result.getOrNull()?.forEach { report ->
                reportDao.insertReport(ReportEntity.fromDomain(report).copy(isSynced = true))
            }
        }
    }

    override fun observeRemoteReports(): Flow<List<Report>> {
        return firebaseRepository.getReportsFlow()
            .onEach { reports ->
                withContext(Dispatchers.IO) {
                    reports.forEach { report ->
                        reportDao.insertReport(ReportEntity.fromDomain(report).copy(isSynced = true))
                    }
                }
            }
    }
}
