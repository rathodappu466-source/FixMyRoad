package com.example.fixmyroad.data.repository

import com.example.fixmyroad.domain.model.Report
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseRepository @Inject constructor() {

    private val firestore = FirebaseFirestore.getInstance()
    private val reportsCollection = firestore.collection("reports")

    suspend fun submitReport(report: Report): Result<Unit> {
        return try {
            reportsCollection
                .document(report.ticketId)
                .set(report)
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getReportsFlow(): Flow<List<Report>> = callbackFlow {
        val subscription = reportsCollection
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val reports = snapshot.toObjects(Report::class.java)
                    trySend(reports)
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun fetchReportsOnce(): Result<List<Report>> {
        return try {
            val snapshot = reportsCollection
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .await()
            val reports = snapshot.toObjects(Report::class.java)
            Result.success(reports)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getReportById(ticketId: String): Flow<Report?> = callbackFlow {
        val subscription = reportsCollection
            .document(ticketId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null && snapshot.exists()) {
                    val report = snapshot.toObject(Report::class.java)
                    trySend(report)
                } else {
                    trySend(null)
                }
            }
        awaitClose { subscription.remove() }
    }

    suspend fun updateReport(report: Report): Result<Unit> {
        return try {
            reportsCollection
                .document(report.ticketId)
                .update(
                    mapOf(
                        "title" to report.title,
                        "description" to report.description,
                        "category" to report.category,
                        "severity" to report.severity,
                        "status" to report.status,
                        "address" to report.address,
                        "city" to report.city,
                        "area" to report.area,
                        "pincode" to report.pincode,
                        "imageUri" to report.imageUri,
                        "latitude" to report.latitude,
                        "longitude" to report.longitude,
                        "adminResponse" to report.adminResponse,
                        "adminUpdatedAt" to report.adminUpdatedAt
                    )
                )
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteReport(ticketId: String): Result<Unit> {
        return try {
            reportsCollection
                .document(ticketId)
                .delete()
                .await()
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
