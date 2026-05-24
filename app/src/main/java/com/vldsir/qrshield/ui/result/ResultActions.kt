package com.vldsir.qrshield.ui.result

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vldsir.qrshield.R
import com.vldsir.qrshield.analysis.risk.Verdict
import com.vldsir.qrshield.analysis.wifi.WifiConfig
import com.vldsir.qrshield.classifier.PayloadType
import com.vldsir.qrshield.data.history.ScanRecord

@Composable
fun ResultActions(
    record: ScanRecord,
    wifiConfig: WifiConfig?,
    onDismiss: () -> Unit,
    onOpenUrl: () -> Unit,
    onCopyLink: () -> Unit,
    onOpenWifiSettings: () -> Unit,
    onCopyPassword: () -> Unit,
    onSaveContact: () -> Unit,
    onComposeEmail: () -> Unit,
    onCopyAddress: () -> Unit,
    onDial: () -> Unit,
    onCopyNumber: () -> Unit,
    onCopyText: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showDangerousDialog by remember { mutableStateOf(false) }
    var pendingDangerousAction by remember { mutableStateOf<(() -> Unit)?>(null) }

    fun primaryAction(action: () -> Unit) {
        if (record.verdict == Verdict.DANGEROUS) {
            pendingDangerousAction = action
            showDangerousDialog = true
        } else {
            action()
        }
    }

    if (showDangerousDialog) {
        AlertDialog(
            onDismissRequest = { showDangerousDialog = false },
            title = { Text(stringResource(R.string.dangerous_continue_title)) },
            text = { Text(stringResource(R.string.dangerous_continue_message)) },
            confirmButton = {
                TextButton(onClick = {
                    showDangerousDialog = false
                    pendingDangerousAction?.invoke()
                }) { Text(stringResource(R.string.continue_action)) }
            },
            dismissButton = {
                TextButton(onClick = { showDangerousDialog = false }) { Text(stringResource(R.string.cancel)) }
            },
        )
    }

    Column(modifier = modifier.fillMaxWidth().padding(top = 8.dp)) {
        when (record.payloadType) {
            PayloadType.URL -> {
                Button(
                    onClick = { primaryAction(onOpenUrl) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.open_in_browser)) }
                OutlinedButton(
                    onClick = onCopyLink,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) { Text(stringResource(R.string.copy_link)) }
            }

            PayloadType.WIFI -> {
                wifiConfig?.let { config ->
                    WifiDetails(config = config, onCopyPassword = onCopyPassword)
                }
                Button(
                    onClick = onOpenWifiSettings,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) { Text(stringResource(R.string.open_wifi_settings)) }
                if (wifiConfig?.password != null) {
                    OutlinedButton(
                        onClick = onCopyPassword,
                        modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                    ) { Text(stringResource(R.string.copy_password)) }
                }
            }

            PayloadType.CONTACT -> {
                Button(
                    onClick = { primaryAction(onSaveContact) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.save_to_contacts)) }
            }

            PayloadType.EMAIL -> {
                Button(
                    onClick = { primaryAction(onComposeEmail) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.compose_email)) }
                OutlinedButton(
                    onClick = onCopyAddress,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) { Text(stringResource(R.string.copy_address)) }
            }

            PayloadType.PHONE -> {
                Button(
                    onClick = { primaryAction(onDial) },
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.dial)) }
                OutlinedButton(
                    onClick = onCopyNumber,
                    modifier = Modifier.fillMaxWidth().padding(top = 8.dp),
                ) { Text(stringResource(R.string.copy_number)) }
            }

            PayloadType.TEXT -> {
                Button(
                    onClick = onCopyText,
                    modifier = Modifier.fillMaxWidth(),
                ) { Text(stringResource(R.string.copy_text)) }
            }
        }

        TextButton(
            onClick = onDismiss,
            modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
        ) { Text(stringResource(R.string.dismiss)) }
    }
}

@Composable
private fun WifiDetails(config: WifiConfig, onCopyPassword: () -> Unit) {
    var passwordVisible by remember { mutableStateOf(false) }

    Column(modifier = Modifier.fillMaxWidth()) {
        WifiDetailRow(label = stringResource(R.string.ssid), value = config.ssid)
        WifiDetailRow(label = stringResource(R.string.encryption), value = config.encryption.name)
        config.password?.let { pwd ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth(),
            ) {
                Text(
                    text = "${stringResource(R.string.password)}: ${if (passwordVisible) pwd else "••••••••"}",
                    style = MaterialTheme.typography.bodyMedium,
                )
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                        contentDescription = if (passwordVisible) stringResource(R.string.hide_password)
                        else stringResource(R.string.show_password),
                    )
                }
            }
        }
        if (config.hidden) {
            Text(
                text = stringResource(R.string.hidden_network),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun WifiDetailRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Text(
            text = "$label: ",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(text = value, style = MaterialTheme.typography.bodyMedium)
    }
}
