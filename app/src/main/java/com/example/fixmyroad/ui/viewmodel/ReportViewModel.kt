package com.example.fixmyroad.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fixmyroad.data.repository.FirebaseUserRepository
import com.example.fixmyroad.domain.model.Report
import com.example.fixmyroad.domain.repository.ReportRepository
import com.example.fixmyroad.domain.repository.UserRepository
import com.example.fixmyroad.utils.DetailedAddress
import com.example.fixmyroad.utils.LocationHelper
import com.google.android.gms.maps.model.LatLng
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Data class to hold form validation state
 */
data class FormValidation(
    val titleError: String? = null,
    val descriptionError: String? = null,
    val categoryError: String? = null,
    val imageError: String? = null,
    val locationError: String? = null,
    val addressError: String? = null,
    val isValid: Boolean = true
)

@HiltViewModel
class ReportViewModel @Inject constructor(
    private val repository: ReportRepository,
    private val userRepository: UserRepository,
    private val firebaseUserRepository: FirebaseUserRepository,
    private val locationHelper: LocationHelper
) : ViewModel() {

    val allReports: StateFlow<List<Report>> = repository.getAllReports()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Form Validation States
    private val _formValidation = MutableStateFlow(FormValidation())
    val formValidation: StateFlow<FormValidation> = _formValidation.asStateFlow()

    private val _isSubmitting = MutableStateFlow(false)
    val isSubmitting: StateFlow<Boolean> = _isSubmitting.asStateFlow()

    private val _submissionError = MutableStateFlow<String?>(null)
    val submissionError: StateFlow<String?> = _submissionError.asStateFlow()

    private val _isRetrying = MutableStateFlow(false)
    val isRetrying: StateFlow<Boolean> = _isRetrying.asStateFlow()

    private val _lastFailedSubmission = MutableStateFlow<LastFailedSubmission?>(null)
    val lastFailedSubmission: StateFlow<LastFailedSubmission?> = _lastFailedSubmission.asStateFlow()

    private val _selectedLocation = MutableStateFlow<LatLng?>(null)
    val selectedLocation: StateFlow<LatLng?> = _selectedLocation.asStateFlow()

    private val _detailedAddress = MutableStateFlow<DetailedAddress?>(null)
    val detailedAddress: StateFlow<DetailedAddress?> = _detailedAddress.asStateFlow()

    private val _isFetchingAddress = MutableStateFlow(false)
    val isFetchingAddress: StateFlow<Boolean> = _isFetchingAddress.asStateFlow()

    init {
        repository.observeRemoteReports()
            .launchIn(viewModelScope)
        
        fetchInitialLocation()
    }

    private fun fetchInitialLocation() {
        viewModelScope.launch {
            val location = locationHelper.getCurrentLocation()
            location?.let {
                val latLng = LatLng(it.latitude, it.longitude)
                updateSelectedLocation(latLng)
            } ?: run {
                updateSelectedLocation(LatLng(12.9716, 77.5946))
            }
        }
    }

    fun updateSelectedLocation(latLng: LatLng) {
        if (_selectedLocation.value == latLng) return
        _selectedLocation.value = latLng
        fetchAddress(latLng.latitude, latLng.longitude)
    }

    private fun fetchAddress(latitude: Double, longitude: Double) {
        viewModelScope.launch {
            _isFetchingAddress.value = true
            try {
                val detailed = locationHelper.getDetailedAddress(latitude, longitude)
                _detailedAddress.value = detailed
                // Clear address error if address is fetched
                _formValidation.value = _formValidation.value.copy(addressError = null)
            } catch (e: Exception) {
                _formValidation.value = _formValidation.value.copy(
                    addressError = "Could not fetch address details"
                )
            } finally {
                _isFetchingAddress.value = false
            }
        }
    }

    fun searchLocation(query: String) {
        viewModelScope.launch {
            val location = locationHelper.searchLocation(query)
            location?.let {
                updateSelectedLocation(LatLng(it.latitude, it.longitude))
            }
        }
    }

    /**
     * Validate form inputs before submission.
     */
    private fun validateForm(
        title: String,
        description: String,
        category: String,
        imageUri: Uri?,
        hasLocation: Boolean,
        hasAddress: Boolean
    ): FormValidation {
        val errors = mutableMapOf<String, String?>()

        // Title validation
        errors["title"] = when {
            title.isBlank() -> "Title is required"
            title.length < 3 -> "Title must be at least 3 characters"
            title.length > 100 -> "Title must not exceed 100 characters"
            else -> null
        }

        // Description validation
        errors["description"] = when {
            description.isBlank() -> "Description is required"
            description.length < 10 -> "Description must be at least 10 characters"
            description.length > 1000 -> "Description must not exceed 1000 characters"
            else -> null
        }

        // Category validation
        errors["category"] = when {
            category.isBlank() -> "Please select a category"
            else -> null
        }

        // Image validation (optional but recommended)
        errors["image"] = if (imageUri == null) {
            "Image evidence is recommended for faster resolution"
        } else {
            null
        }

        // Location validation
        errors["location"] = if (!hasLocation) {
            "Please select a location on the map"
        } else {
            null
        }

        // Address validation
        errors["address"] = if (!hasAddress) {
            "Could not determine the precise address. Try another location."
        } else {
            null
        }

        val isValid = errors.values.all { it == null }

        return FormValidation(
            titleError = errors["title"],
            descriptionError = errors["description"],
            categoryError = errors["category"],
            imageError = errors["image"],
            locationError = errors["location"],
            addressError = errors["address"],
            isValid = isValid
        )
    }

    /**
     * Submits a report with full validation and error handling.
     * Includes retry capability and proper error states.
     */
    fun submitReport(
        title: String,
        description: String,
        category: String,
        severity: String,
        imageUri: Uri?,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val location = _selectedLocation.value
        val address = _detailedAddress.value
        val hasLocation = location != null
        val hasAddress = !address?.fullAddress.isNullOrEmpty()

        // Validate form
        val validation = validateForm(
            title = title,
            description = description,
            category = category,
            imageUri = imageUri,
            hasLocation = hasLocation,
            hasAddress = hasAddress
        )

        _formValidation.value = validation

        if (!validation.isValid) {
            onError("Please fix the errors and try again")
            return
        }

        viewModelScope.launch {
            _isSubmitting.value = true
            _submissionError.value = null

            try {
                val detailed = _detailedAddress.value
                val timestamp = System.currentTimeMillis()

                // Get current user info for report ownership
                val currentUserId = firebaseUserRepository.getCurrentUserId()
                val currentUser = if (currentUserId != null) {
                    userRepository.getUserById(currentUserId)
                } else {
                    null
                }

                val report = Report(
                    ticketId = "TKT-${timestamp % 100000}-${(10..99).random()}",
                    title = title,
                    description = description,
                    category = category,
                    severity = severity,
                    latitude = location!!.latitude,
                    longitude = location.longitude,
                    address = detailed?.fullAddress,
                    city = detailed?.city,
                    area = detailed?.area,
                    pincode = detailed?.pincode,
                    imageUri = null,
                    timestamp = timestamp,
                    status = "Pending",
                    isSynced = false,
                    userId = currentUserId ?: "",
                    userName = currentUser?.fullName ?: "Anonymous",
                    userEmail = currentUser?.email ?: ""
                )

                // Store failed submission for retry
                _lastFailedSubmission.value = LastFailedSubmission(
                    report = report,
                    imageUri = imageUri
                )

                repository.submitReport(report, imageUri)

                _submissionError.value = null
                _lastFailedSubmission.value = null
                onSuccess()
            } catch (e: Exception) {
                val errorMsg = handleSubmissionError(e)
                _submissionError.value = errorMsg
                onError(errorMsg)
            } finally {
                _isSubmitting.value = false
            }
        }
    }

    /**
     * Retry failed report submission.
     */
    fun retrySubmission(
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        val lastSubmission = _lastFailedSubmission.value ?: run {
            onError("No failed submission to retry")
            return
        }

        viewModelScope.launch {
            _isRetrying.value = true
            _submissionError.value = null

            try {
                delay(500) // Small delay for UX smoothness

                repository.submitReport(lastSubmission.report, lastSubmission.imageUri)

                _submissionError.value = null
                _lastFailedSubmission.value = null
                onSuccess()
            } catch (e: Exception) {
                val errorMsg = handleSubmissionError(e)
                _submissionError.value = errorMsg
                onError(errorMsg)
            } finally {
                _isRetrying.value = false
            }
        }
    }

    /**
     * Handle different types of submission errors with user-friendly messages.
     */
    private fun handleSubmissionError(exception: Exception): String {
        return when {
            exception.message?.contains("network", ignoreCase = true) == true ->
                "Network error. Check your connection and try again."
            exception.message?.contains("timeout", ignoreCase = true) == true ->
                "Request timed out. Please try again."
            exception.message?.contains("permission", ignoreCase = true) == true ->
                "Permission denied. Please check app permissions."
            exception.message?.contains("storage", ignoreCase = true) == true ->
                "Failed to upload image. The file might be too large or corrupted."
            exception.message?.contains("firestore", ignoreCase = true) == true ->
                "Failed to save report to server. Please try again."
            else -> exception.message ?: "Failed to submit report. Please try again."
        }
    }

    fun clearValidationErrors() {
        _formValidation.value = FormValidation()
    }

    fun clearSubmissionError() {
        _submissionError.value = null
    }
}

data class LastFailedSubmission(
    val report: Report,
    val imageUri: Uri?
)
