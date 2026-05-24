package com.vldsir.qrshield.data.preferences

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SettingsRepository(context: Context) {

    private val prefs: SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    // ── Theme ──────────────────────────────────────────────────────────────────

    private val _themeFlow = MutableStateFlow(loadTheme())
    val themeFlow: StateFlow<AppTheme> = _themeFlow.asStateFlow()

    fun setTheme(theme: AppTheme) {
        prefs.edit().putString(KEY_THEME, theme.name).apply()
        _themeFlow.value = theme
    }

    private fun loadTheme(): AppTheme = try {
        AppTheme.valueOf(prefs.getString(KEY_THEME, AppTheme.SYSTEM.name) ?: AppTheme.SYSTEM.name)
    } catch (_: IllegalArgumentException) {
        AppTheme.SYSTEM
    }

    // ── Tutorial ───────────────────────────────────────────────────────────────

    fun isTutorialCompleted(): Boolean =
        prefs.getBoolean(KEY_TUTORIAL_DONE, false)

    fun markTutorialCompleted() {
        prefs.edit().putBoolean(KEY_TUTORIAL_DONE, true).apply()
    }

    fun resetTutorial() {
        prefs.edit().putBoolean(KEY_TUTORIAL_DONE, false).apply()
    }

    // ── Constants ──────────────────────────────────────────────────────────────

    private companion object {
        const val PREFS_NAME = "qrshield_settings"
        const val KEY_THEME = "theme"
        const val KEY_TUTORIAL_DONE = "tutorial_completed"
    }
}
