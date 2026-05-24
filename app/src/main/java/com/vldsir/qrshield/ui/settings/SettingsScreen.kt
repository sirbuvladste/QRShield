package com.vldsir.qrshield.ui.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.Replay
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vldsir.qrshield.R
import com.vldsir.qrshield.data.preferences.AppTheme

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onNavigateBack: () -> Unit,
    onNavigateToTutorial: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.navEvents.collect { event ->
            when (event) {
                is SettingsNavEvent.NavigateToTutorial -> onNavigateToTutorial()
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.navigate_back),
                        )
                    }
                },
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp),
        ) {
            Spacer(Modifier.height(8.dp))

            // ── Appearance ──────────────────────────────────────────────────
            Text(
                text = stringResource(R.string.settings_section_appearance),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.settings_theme),
                style = MaterialTheme.typography.bodyLarge,
            )

            Spacer(Modifier.height(8.dp))

            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth()) {
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = 0, count = 3),
                    selected = uiState.selectedTheme == AppTheme.SYSTEM,
                    onClick = { viewModel.setTheme(AppTheme.SYSTEM) },
                    icon = {
                        SegmentedButtonDefaults.Icon(
                            active = uiState.selectedTheme == AppTheme.SYSTEM,
                            inactiveContent = {
                                Icon(
                                    Icons.Filled.Brightness4,
                                    contentDescription = null,
                                    modifier = Modifier.size(SegmentedButtonDefaults.IconSize),
                                )
                            },
                        )
                    },
                    label = { Text(stringResource(R.string.theme_system)) },
                )
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = 1, count = 3),
                    selected = uiState.selectedTheme == AppTheme.LIGHT,
                    onClick = { viewModel.setTheme(AppTheme.LIGHT) },
                    icon = {
                        SegmentedButtonDefaults.Icon(
                            active = uiState.selectedTheme == AppTheme.LIGHT,
                            inactiveContent = {
                                Icon(
                                    Icons.Filled.LightMode,
                                    contentDescription = null,
                                    modifier = Modifier.size(SegmentedButtonDefaults.IconSize),
                                )
                            },
                        )
                    },
                    label = { Text(stringResource(R.string.theme_light)) },
                )
                SegmentedButton(
                    shape = SegmentedButtonDefaults.itemShape(index = 2, count = 3),
                    selected = uiState.selectedTheme == AppTheme.DARK,
                    onClick = { viewModel.setTheme(AppTheme.DARK) },
                    icon = {
                        SegmentedButtonDefaults.Icon(
                            active = uiState.selectedTheme == AppTheme.DARK,
                            inactiveContent = {
                                Icon(
                                    Icons.Filled.DarkMode,
                                    contentDescription = null,
                                    modifier = Modifier.size(SegmentedButtonDefaults.IconSize),
                                )
                            },
                        )
                    },
                    label = { Text(stringResource(R.string.theme_dark)) },
                )
            }

            Spacer(Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(Modifier.height(24.dp))

            // ── Tutorial ────────────────────────────────────────────────────
            Text(
                text = stringResource(R.string.settings_section_tutorial),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
            )

            Spacer(Modifier.height(12.dp))

            Text(
                text = stringResource(R.string.settings_tutorial_description),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(12.dp))

            Button(
                onClick = { viewModel.resetTutorial() },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    contentColor = MaterialTheme.colorScheme.onSecondaryContainer,
                ),
            ) {
                Icon(
                    imageVector = Icons.Filled.Replay,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp),
                )
                Spacer(Modifier.width(8.dp))
                Text(stringResource(R.string.settings_replay_tutorial))
            }

            Spacer(Modifier.height(24.dp))
        }
    }
}

