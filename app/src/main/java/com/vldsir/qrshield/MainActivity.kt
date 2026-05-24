package com.vldsir.qrshield

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.runtime.getValue
import com.vldsir.qrshield.data.preferences.AppTheme
import com.vldsir.qrshield.navigation.QRShieldNavHost
import com.vldsir.qrshield.ui.theme.QRShieldTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val container = (application as QRShieldApplication).container
        setContent {
            val theme by container.settingsRepository.themeFlow.collectAsStateWithLifecycle()
            val systemDark = isSystemInDarkTheme()
            val darkTheme = when (theme) {
                AppTheme.LIGHT -> false
                AppTheme.DARK -> true
                AppTheme.SYSTEM -> systemDark
            }
            QRShieldTheme(darkTheme = darkTheme) {
                QRShieldNavHost(container = container)
            }
        }
    }
}
