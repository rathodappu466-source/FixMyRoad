package com.example.fixmyroad.domain.repository

import com.example.fixmyroad.domain.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getCurrentUser(): Flow<User?>
    suspend fun createUserProfile(user: User)
    suspend fun updateUserProfile(user: User)
    suspend fun getUserById(uid: String): User?
    fun observeUserProfile(uid: String): Flow<User?>
}
