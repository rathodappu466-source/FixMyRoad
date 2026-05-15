package com.example.fixmyroad.data

import com.example.fixmyroad.Report
import com.example.fixmyroad.ReportDao
import com.example.fixmyroad.network.ApiService
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ReportRepository(
    private val reportDao: ReportDao,
    private val apiService: ApiService
) {

    private val firestore = FirebaseFirestore.getInstance()

    // Get reports from Room
    fun getAllReports() = reportDao.getAllReports()

    // Submit report
    suspend fun submitReport(report: Report) = withContext(Dispatchers.IO) {

        try {

            // Save locally
            reportDao.insertReport(report)

            // Upload to Firebase
            firestore.collection("reports")
                .add(report)
                .await()

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    // Sync Firebase reports into Room
    suspend fun syncReportsFromFirebase() = withContext(Dispatchers.IO) {

        try {

            val snapshot = firestore.collection("reports")
                .get()
                .await()

            val reports = snapshot.documents.mapNotNull {
                it.toObject(Report::class.java)
            }

            reports.forEach { report ->
                reportDao.insertReport(report)
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}