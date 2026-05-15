package com.example.fixmyroad

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reports")
data class Report(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ticketId: String,
    val description: String,
    val category: String,
    val latitude: Double,
    val longitude: Double,
    val imageUri: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val status: String = "Pending"
)
