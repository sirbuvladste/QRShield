package com.vldsir.qrshield.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.vldsir.qrshield.data.preferences.AppTheme
import com.vldsir.qrshield.data.preferences.SettingsRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class SettingsUiState(
    val selectedTheme: AppTheme = AppTheme.SYSTEM,
)

sealed interface SettingsNavEvent {
    data object NavigateToTutorial : SettingsNavEvent
}

class SettingsViewModel(
    private val settingsRepository: SettingsRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(
        SettingsUiState(selectedTheme = settingsRepository.themeFlow.value)
    )
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    private val _navEvents = MutableSharedFlow<SettingsNavEvent>()
    val navEvents: SharedFlow<SettingsNavEvent> = _navEvents.asSharedFlow()

    fun setTheme(theme: AppTheme) {
        settingsRepository.setTheme(theme)
        _uiState.update { it.copy(selectedTheme = theme) }
    }

    fun resetTutorial() {
        settingsRepository.resetTutorial()
        viewModelScope.launch {
            _navEvents.emit(SettingsNavEvent.NavigateToTutorial)
        }
    }
}
