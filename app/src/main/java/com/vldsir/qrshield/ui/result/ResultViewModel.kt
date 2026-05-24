package com.vldsir.qrshield.ui.result

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vldsir.qrshield.data.history.ScanHistoryRepository
import com.vldsir.qrshield.data.history.ScanRecord
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ResultUiState(
    val loading: Boolean = true,
    val record: ScanRecord? = null,
    val errorMessage: String? = null,
)

class ResultViewModel(
    private val scanId: Long,
    private val historyRepository: ScanHistoryRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ResultUiState())
    val uiState: StateFlow<ResultUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            historyRepository.observeById(scanId).collect { record ->
                _uiState.update {
                    it.copy(
                        loading = false,
                        record = record,
                        errorMessage = if (record == null) "Scan not found" else null,
                    )
                }
            }
        }
    }
}
