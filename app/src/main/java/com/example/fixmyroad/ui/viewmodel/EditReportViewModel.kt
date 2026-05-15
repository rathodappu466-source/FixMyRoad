package com.example.fixmyroad.ui.viewmodel

import android.net.Uri
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.fixmyroad.domain.model.Report
import com.example.fixmyroad.domain.repository.ReportRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class EditReportViewModel @Inject constructor(
    private val repository: ReportRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val ticketId: String = checkNotNull(savedStateHandle["ticketId"])

    private val _report = MutableStateFlow<Report?>(null)
    val report: StateFlow<Report?> = _report.asStateFlow()

    private val _title = MutableStateFlow("")
    val title: StateFlow<String> = _title.asStateFlow()

    private val _description = MutableStateFlow("")
    val description: StateFlow<String> = _description.asStateFlow()

    private val _category = MutableStateFlow("")
    val category: StateFlow<String> = _category.asStateFlow()

    private val _severity = MutableStateFlow("")
    val severity: StateFlow<String> = _severity.asStateFlow()

    private val _selectedImageUri = MutableStateFlow<Uri?>(null)
    val selectedImageUri: StateFlow<Uri?> = _selectedImageUri.asStateFlow()

    private val _isUpdating = MutableStateFlow(false)
    val isUpdating: StateFlow<Boolean> = _isUpdating.asStateFlow()

    private val _updateSuccess = MutableStateFlow(false)
    val updateSuccess: StateFlow<Boolean> = _updateSuccess.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    init {
        loadReport()
    }

    private fun loadReport() {
        viewModelScope.launch {
            repository.getReportById(ticketId).collect { report ->
                report?.let {
                    _report.value = it
                    _title.value = it.title
                    _description.value = it.description
                    _category.value = it.category
                    _severity.value = it.severity
                }
            }
        }
    }

    fun updateTitle(newTitle: String) { _title.value = newTitle }
    fun updateDescription(newDesc: String) { _description.value = newDesc }
    fun updateCategory(newCat: String) { _category.value = newCat }
    fun updateSeverity(newSev: String) { _severity.value = newSev }
    fun updateImageUri(uri: Uri) { _selectedImageUri.value = uri }

    fun submitUpdate(onSuccess: () -> Unit, onError: (String) -> Unit) {
        val currentReport = _report.value ?: return
        
        viewModelScope.launch {
            _isUpdating.value = true
            _error.value = null
            try {
                val updatedReport = currentReport.copy(
                    title = _title.value,
                    description = _description.value,
                    category = _category.value,
                    severity = _severity.value
                )
                repository.updateReport(updatedReport, _selectedImageUri.value)
                _updateSuccess.value = true
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to update report"
                onError(_error.value!!)
            } finally {
                _isUpdating.value = false
            }
        }
    }

    fun resetUpdateSuccess() { _updateSuccess.value = false }
    fun clearError() { _error.value = null }
}
