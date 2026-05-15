package com.example.fixmyroad.domain.model

data class User(
    val uid: String = "",
    val fullName: String = "",
    val email: String = "",
    val phoneNumber: String? = "",
    val profileImageUrl: String? = "",
    val createdAt: Long = System.currentTimeMillis()
)
