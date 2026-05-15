package com.example.fixmyroad.data.repository

import com.example.fixmyroad.domain.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirebaseUserRepository @Inject constructor() {

    private val firestore = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val usersCollection =
        firestore.collection("users")

    fun getCurrentUser(): Flow<User?> = callbackFlow {

        val currentUser = auth.currentUser

        if (currentUser != null) {

            val subscription = usersCollection
                .document(currentUser.uid)
                .addSnapshotListener { snapshot, error ->

                    if (error != null) {
                        close(error)
                        return@addSnapshotListener
                    }

                    if (snapshot != null && snapshot.exists()) {

                        val user =
                            snapshot.toObject(User::class.java)

                        trySend(user)

                    } else {

                        trySend(null)
                    }
                }

            awaitClose {
                subscription.remove()
            }

        } else {

            trySend(null)
            close()
        }
    }

    suspend fun createUserProfile(
        user: User
    ): Result<Unit> {

        return try {

            usersCollection
                .document(user.uid)
                .set(user)
                .await()

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun updateUserProfile(
        user: User
    ): Result<Unit> {

        return try {

            usersCollection
                .document(user.uid)
                .update(
                    mapOf(
                        "fullName" to user.fullName,
                        "email" to user.email
                    )
                )
                .await()

            Result.success(Unit)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    suspend fun getUserById(
        uid: String
    ): Result<User?> {

        return try {

            val snapshot = usersCollection
                .document(uid)
                .get()
                .await()

            val user =
                if (snapshot.exists()) {
                    snapshot.toObject(User::class.java)
                } else {
                    null
                }

            Result.success(user)

        } catch (e: Exception) {

            Result.failure(e)
        }
    }

    fun observeUserProfile(
        uid: String
    ): Flow<User?> = callbackFlow {

        val subscription = usersCollection
            .document(uid)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {

                    val user =
                        snapshot.toObject(User::class.java)

                    trySend(user)

                } else {

                    trySend(null)
                }
            }

        awaitClose {
            subscription.remove()
        }
    }

    fun getCurrentUserId(): String? {
        return auth.currentUser?.uid
    }
}