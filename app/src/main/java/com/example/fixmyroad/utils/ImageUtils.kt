package com.example.fixmyroad.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

/**
 * Utility for safe bitmap handling and compression.
 * Prevents memory leaks and OutOfMemoryErrors by using proper scaling.
 */
object ImageUtils {

    /**
     * Scales and compresses an image from a Uri into a ByteArray.
     * @param context Application context
     * @param uri URI of the image to compress
     * @param maxWidth Maximum width for the scaled image
     * @param maxHeight Maximum height for the scaled image
     * @return Compressed image data as ByteArray, or null if processing failed
     */
    suspend fun compressImage(
        context: Context,
        uri: Uri,
        maxWidth: Int = 1080,
        maxHeight: Int = 1080
    ): ByteArray? = withContext(Dispatchers.IO) {
        try {
            val contentResolver = context.contentResolver
            
            // 1. Check dimensions without loading into memory
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = true
            }
            contentResolver.openInputStream(uri)?.use { 
                BitmapFactory.decodeStream(it, null, options) 
            }

            // 2. Calculate optimal sample size
            options.inSampleSize = calculateInSampleSize(options, maxWidth, maxHeight)
            options.inJustDecodeBounds = false

            // 3. Decode scaled bitmap
            val scaledBitmap = contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, options)
            } ?: return@withContext null

            // 4. Compress to JPEG (80% quality)
            val outputStream = ByteArrayOutputStream()
            scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val byteArray = outputStream.toByteArray()
            
            // 5. Clean up memory
            scaledBitmap.recycle()
            
            byteArray
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun calculateInSampleSize(options: BitmapFactory.Options, reqWidth: Int, reqHeight: Int): Int {
        val (height: Int, width: Int) = options.outHeight to options.outWidth
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {
            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2
            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }
}
