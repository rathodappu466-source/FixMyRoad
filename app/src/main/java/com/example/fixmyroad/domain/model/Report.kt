package com.example.fixmyroad.domain.model

data class Report(

    val id: Long = 0,

    val ticketId: String = "",

    val title: String = "",

    val description: String = "",

    val category: String = "",

    val severity: String = "Medium",

    val latitude: Double = 0.0,

    val longitude: Double = 0.0,

    val address: String? = null,

    val city: String? = null,

    val area: String? = null,

    val pincode: String? = null,

    val imageUri: String? = null,

    val timestamp: Long = System.currentTimeMillis(),

    val status: String = "Pending",

    val isSynced: Boolean = false,

    val userId: String = "",

    val userName: String = "",

    val userEmail: String = ""
)