package com.example.fixmyroad.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.fixmyroad.domain.model.Report

@Entity(tableName = "reports")
data class ReportEntity(
    @PrimaryKey val ticketId: String,
    val title: String,
    val description: String,
    val category: String,
    val severity: String,
    val latitude: Double,
    val longitude: Double,
    val address: String?,
    val city: String?,
    val area: String?,
    val pincode: String?,
    val imageUri: String?,
    val timestamp: Long,
    val status: String,
    val isSynced: Boolean,
    val userId: String,
    val userName: String,
    val userEmail: String,
    val adminResponse: String?,
    val adminUpdatedAt: Long?
) {
    fun toDomain() = Report(
        ticketId = ticketId,
        title = title,
        description = description,
        category = category,
        severity = severity,
        latitude = latitude,
        longitude = longitude,
        address = address,
        city = city,
        area = area,
        pincode = pincode,
        imageUri = imageUri,
        timestamp = timestamp,
        status = status,
        isSynced = isSynced,
        userId = userId,
        userName = userName,
        userEmail = userEmail,
        adminResponse = adminResponse,
        adminUpdatedAt = adminUpdatedAt
    )

    companion object {
        fun fromDomain(report: Report) = ReportEntity(
            ticketId = report.ticketId,
            title = report.title,
            description = report.description,
            category = report.category,
            severity = report.severity,
            latitude = report.latitude,
            longitude = report.longitude,
            address = report.address,
            city = report.city,
            area = report.area,
            pincode = report.pincode,
            imageUri = report.imageUri,
            timestamp = report.timestamp,
            status = report.status,
            isSynced = report.isSynced,
            userId = report.userId,
            userName = report.userName,
            userEmail = report.userEmail,
            adminResponse = report.adminResponse,
            adminUpdatedAt = report.adminUpdatedAt
        )
    }
}
