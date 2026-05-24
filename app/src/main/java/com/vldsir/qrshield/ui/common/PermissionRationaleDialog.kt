package com.vldsir.qrshield.ui.common

import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.vldsir.qrshield.R

@Composable
fun PermissionRationaleDialog(
    onRetry: () -> Unit,
    onOpenSettings: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.app_name)) },
        text = { Text(stringResource(R.string.permission_rationale)) },
        confirmButton = {
            TextButton(onClick = onRetry) { Text(stringResource(R.string.retry)) }
        },
        dismissButton = {
            TextButton(onClick = onOpenSettings) { Text(stringResource(R.string.open_settings)) }
        },
    )
}
