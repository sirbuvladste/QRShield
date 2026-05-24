package com.vldsir.qrshield.ui.history

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vldsir.qrshield.R
import com.vldsir.qrshield.analysis.risk.Verdict
import com.vldsir.qrshield.classifier.PayloadType
import com.vldsir.qrshield.data.history.ScanRecord
import com.vldsir.qrshield.ui.theme.OnDangerousContainer
import com.vldsir.qrshield.ui.theme.OnSafeContainer
import com.vldsir.qrshield.ui.theme.OnSuspiciousContainer

private data class VerdictIcon(val icon: ImageVector, val tint: Color)

@Composable
fun HistoryItem(
    record: ScanRecord,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val verdictIcon: VerdictIcon = when (record.verdict) {
        Verdict.SAFE -> VerdictIcon(Icons.Filled.CheckCircle, OnSafeContainer)
        Verdict.SUSPICIOUS -> VerdictIcon(Icons.Filled.Warning, OnSuspiciousContainer)
        Verdict.DANGEROUS -> VerdictIcon(Icons.Filled.Dangerous, OnDangerousContainer)
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
    ) {
        Icon(
            imageVector = verdictIcon.icon,
            contentDescription = record.verdict.name,
            tint = verdictIcon.tint,
            modifier = Modifier.size(20.dp),
        )
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            val typeLabel = when (record.payloadType) {
                PayloadType.URL -> stringResource(R.string.payload_type_url)
                PayloadType.WIFI -> stringResource(R.string.payload_type_wifi)
                PayloadType.CONTACT -> stringResource(R.string.payload_type_contact)
                PayloadType.EMAIL -> stringResource(R.string.payload_type_email)
                PayloadType.PHONE -> stringResource(R.string.payload_type_phone)
                PayloadType.TEXT -> stringResource(R.string.payload_type_text)
            }
            AssistChip(onClick = {}, label = { Text(typeLabel, style = MaterialTheme.typography.labelSmall) })
            Text(
                text = record.payload.take(80).let { if (record.payload.length > 80) "$it…" else it },
                style = MaterialTheme.typography.bodySmall,
                maxLines = 1,
                modifier = Modifier.padding(top = 2.dp),
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = relativeTime(record.timestamp),
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

private fun relativeTime(epochMs: Long): String {
    val diffMs = System.currentTimeMillis() - epochMs
    return when {
        diffMs < 60_000 -> "just now"
        diffMs < 3_600_000 -> "${diffMs / 60_000} min ago"
        diffMs < 86_400_000 -> "${diffMs / 3_600_000} h ago"
        diffMs < 172_800_000 -> "Yesterday"
        else -> "${diffMs / 86_400_000} days ago"
    }
}
