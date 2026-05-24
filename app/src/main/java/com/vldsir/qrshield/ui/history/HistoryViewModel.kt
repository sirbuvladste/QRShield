package com.vldsir.qrshield.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vldsir.qrshield.data.history.ScanHistoryRepository
import com.vldsir.qrshield.data.history.ScanRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HistoryUiState(val items: List<ScanRecord> = emptyList())

class HistoryViewModel(
    private val historyRepository: ScanHistoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(HistoryUiState())
    val uiState: StateFlow<HistoryUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            historyRepository.observeAll().collect { records ->
                _uiState.update { it.copy(items = records) }
            }
        }
    }

    fun clearHistory() {
        viewModelScope.launch {
            historyRepository.clear()
        }
    }
}
