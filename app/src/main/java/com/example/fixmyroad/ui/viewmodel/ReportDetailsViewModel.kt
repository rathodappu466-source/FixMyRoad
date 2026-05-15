package com.example.fixmyroad.ui.viewmodel

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
class ReportDetailsViewModel @Inject constructor(
    private val repository: ReportRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val ticketId: String = checkNotNull(savedStateHandle["ticketId"])

    val report: StateFlow<Report?> = repository.getReportById(ticketId)
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _isDeleting = MutableStateFlow(false)
    val isDeleting: StateFlow<Boolean> = _isDeleting.asStateFlow()

    private val _deleteSuccess = MutableStateFlow(false)
    val deleteSuccess: StateFlow<Boolean> = _deleteSuccess.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    fun deleteReport(onSuccess: () -> Unit, onError: (String) -> Unit) {
        viewModelScope.launch {
            _isDeleting.value = true
            _error.value = null
            try {
                repository.deleteReport(ticketId)
                _deleteSuccess.value = true
                onSuccess()
            } catch (e: Exception) {
                _error.value = e.message ?: "Failed to delete report"
                onError(_error.value!!)
            } finally {
                _isDeleting.value = false
            }
        }
    }

    fun resetDeleteSuccess() { _deleteSuccess.value = false }
}
