package com.vldsir.qrshield.ui.result

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.vldsir.qrshield.R
import com.vldsir.qrshield.analysis.wifi.WifiQrParser
import com.vldsir.qrshield.classifier.PayloadType
import com.vldsir.qrshield.domain.IntentLauncher
import com.vldsir.qrshield.ui.common.LoadingIndicator
import com.vldsir.qrshield.ui.common.RiskBadge

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(
    viewModel: ResultViewModel,
    intentLauncher: IntentLauncher,
    wifiParser: WifiQrParser,
    onNavigateBack: () -> Unit,
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.app_name)) },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.dismiss))
                    }
                },
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) { Snackbar(it) } },
    ) { innerPadding ->
        when {
            uiState.loading -> LoadingIndicator(modifier = Modifier.padding(innerPadding))
            uiState.record != null -> {
                val record = uiState.record!!
                val wifiConfig = if (record.payloadType == PayloadType.WIFI) wifiParser.parse(record.payload) else null

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                        .verticalScroll(rememberScrollState())
                        .padding(16.dp),
                ) {
                    // 1. Risk badge card
                    RiskBadge(
                        verdict = record.verdict,
                        modifier = Modifier.fillMaxWidth(),
                    )

                    Spacer(Modifier.height(8.dp))

                    // Headline
                    Text(
                        text = record.explanation.headline,
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.padding(vertical = 4.dp),
                    )

                    Spacer(Modifier.height(8.dp))

                    // 2. Payload preview card
                    PayloadPreview(
                        payload = record.payload,
                        payloadType = record.payloadType,
                        onCopy = {
                            try {
                                intentLauncher.copyToClipboard("URL", record.payload)
                            } catch (e: Exception) {
                                // silently fail
                            }
                        },
                    )

                    Spacer(Modifier.height(8.dp))

                    // 3. Explanation card
                    if (record.explanation.bullets.isNotEmpty()) {
                        Card(modifier = Modifier.fillMaxWidth()) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Text(
                                    text = "Analysis",
                                    style = MaterialTheme.typography.titleSmall,
                                )
                                Spacer(Modifier.height(8.dp))
                                record.explanation.bullets.forEach { bullet ->
                                    Text(
                                        text = "• $bullet",
                                        style = MaterialTheme.typography.bodySmall,
                                        modifier = Modifier.padding(vertical = 2.dp),
                                    )
                                }
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                    }

                    // 4. Online lookup indicator
                    Text(
                        text = if (record.onlineLookupUsed)
                            stringResource(R.string.online_reputation_checked)
                        else
                            stringResource(R.string.online_reputation_skipped),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )

                    Spacer(Modifier.height(8.dp))

                    // 5. Actions section
                    ResultActions(
                        record = record,
                        wifiConfig = wifiConfig,
                        onDismiss = onNavigateBack,
                        onOpenUrl = {
                            try { intentLauncher.openUrl(record.payload) }
                            catch (_: Exception) { }
                        },
                        onCopyLink = { intentLauncher.copyToClipboard("URL", record.payload) },
                        onOpenWifiSettings = { intentLauncher.openWifiSettings() },
                        onCopyPassword = {
                            wifiConfig?.password?.let {
                                intentLauncher.copyToClipboard("WiFi Password", it, isSensitive = true)
                            }
                        },
                        onSaveContact = {
                            try { intentLauncher.addContact(record.payload) }
                            catch (_: Exception) { }
                        },
                        onComposeEmail = {
                            val addr = record.payload.removePrefix("mailto:")
                            try { intentLauncher.sendEmail(addr, null, null) }
                            catch (_: Exception) { }
                        },
                        onCopyAddress = {
                            val addr = record.payload.removePrefix("mailto:")
                            intentLauncher.copyToClipboard("Email", addr)
                        },
                        onDial = {
                            try { intentLauncher.dial(record.payload) }
                            catch (_: Exception) { }
                        },
                        onCopyNumber = {
                            val number = record.payload.removePrefix("tel:")
                            intentLauncher.copyToClipboard("Phone", number)
                        },
                        onCopyText = { intentLauncher.copyToClipboard("Text", record.payload) },
                    )
                }
            }
            else -> {
                Text(
                    text = uiState.errorMessage ?: "Error",
                    modifier = Modifier.padding(innerPadding).padding(16.dp),
                )
            }
        }
    }
}
