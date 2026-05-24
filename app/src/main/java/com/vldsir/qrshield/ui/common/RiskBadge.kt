package com.vldsir.qrshield.ui.common

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Dangerous
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vldsir.qrshield.R
import com.vldsir.qrshield.analysis.risk.Verdict
import com.vldsir.qrshield.ui.theme.DangerousContainer
import com.vldsir.qrshield.ui.theme.OnDangerousContainer
import com.vldsir.qrshield.ui.theme.OnSafeContainer
import com.vldsir.qrshield.ui.theme.OnSuspiciousContainer
import com.vldsir.qrshield.ui.theme.SafeContainer
import com.vldsir.qrshield.ui.theme.SuspiciousContainer

@Composable
fun RiskBadge(verdict: Verdict, modifier: Modifier = Modifier) {
    val (containerColor, contentColor, icon, label) = when (verdict) {
        Verdict.SAFE -> BadgeStyle(SafeContainer, OnSafeContainer, Icons.Filled.CheckCircle, stringResource(R.string.verdict_safe))
        Verdict.SUSPICIOUS -> BadgeStyle(SuspiciousContainer, OnSuspiciousContainer, Icons.Filled.Warning, stringResource(R.string.verdict_suspicious))
        Verdict.DANGEROUS -> BadgeStyle(DangerousContainer, OnDangerousContainer, Icons.Filled.Dangerous, stringResource(R.string.verdict_dangerous))
    }
    Surface(color = containerColor, shape = MaterialTheme.shapes.medium, modifier = modifier) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = contentColor,
                modifier = Modifier.size(32.dp),
            )
            Text(
                text = label,
                color = contentColor,
                style = MaterialTheme.typography.titleLarge,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
    }
}

private data class BadgeStyle(
    val containerColor: Color,
    val contentColor: Color,
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    val label: String,
)
