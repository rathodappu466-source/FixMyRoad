package com.example.fixmyroad.data.repository

import android.content.Context
import android.net.Uri
import com.example.fixmyroad.utils.ImageUtils
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.storageMetadata
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Repository for handling Firebase Storage operations.
 * Manages image uploads with compression and URL generation.
 */
@Singleton
class FirebaseStorageRepository @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private val storage = FirebaseStorage.getInstance()
    private val storageRef = storage.reference.child("reports")

    /**
     * Compresses and uploads an image to Firebase Storage.
     * Returns the permanent download URL on success.
     */
    suspend fun uploadReportImage(uri: Uri): Result<String> = withContext(Dispatchers.IO) {
        try {
            // 1. Process and compress image to prevent high data usage and memory crashes
            val imageData = ImageUtils.compressImage(context, uri)
                ?: return@withContext Result.failure(Exception("Failed to process image evidence"))

            // 2. Generate a unique filename to avoid collisions
            val fileName = "IMG_${UUID.randomUUID()}_${System.currentTimeMillis()}.jpg"
            val fileRef = storageRef.child(fileName)

            // 3. Set metadata
            val metadata = storageMetadata {
                contentType = "image/jpeg"
                setCustomMetadata("uploaded_at", System.currentTimeMillis().toString())
            }

            // 4. Perform upload
            fileRef.putBytes(imageData, metadata).await()

            // 5. Retrieve permanent download URL
            val downloadUrl = fileRef.downloadUrl.await().toString()
            Result.success(downloadUrl)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
