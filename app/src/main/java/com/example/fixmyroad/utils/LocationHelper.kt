package com.example.fixmyroad.utils

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import java.util.Locale
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume

class LocationHelper(private val context: Context) {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    @SuppressLint("MissingPermission")
    suspend fun getCurrentLocation(): Location? = withTimeoutOrNull(5000) {
        suspendCancellableCoroutine { continuation ->
            val cancellationTokenSource = CancellationTokenSource()
            fusedLocationClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                cancellationTokenSource.token
            ).addOnSuccessListener { location ->
                if (continuation.isActive) continuation.resume(location)
            }.addOnFailureListener {
                if (continuation.isActive) continuation.resume(null)
            }.addOnCanceledListener {
                if (continuation.isActive) continuation.resume(null)
            }

            continuation.invokeOnCancellation {
                cancellationTokenSource.cancel()
            }
        }
    }

    suspend fun getDetailedAddress(latitude: Double, longitude: Double): DetailedAddress? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val addressLines = (0..address.maxAddressLineIndex).map { address.getAddressLine(it) }
                DetailedAddress(
                    fullAddress = addressLines.joinToString(", "),
                    city = address.locality ?: address.subAdminArea ?: "",
                    area = address.subLocality ?: address.thoroughfare ?: "",
                    pincode = address.postalCode ?: ""
                )
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    suspend fun searchLocation(query: String): Location? = withContext(Dispatchers.IO) {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses = geocoder.getFromLocationName(query, 1)
            if (!addresses.isNullOrEmpty()) {
                val address = addresses[0]
                val location = Location("search")
                location.latitude = address.latitude
                location.longitude = address.longitude
                location
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }
}

data class DetailedAddress(
    val fullAddress: String,
    val city: String,
    val area: String,
    val pincode: String
)
