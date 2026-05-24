package com.vldsir.qrshield.ui.result

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material3.AssistChip
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import com.vldsir.qrshield.classifier.PayloadType

@Composable
fun PayloadPreview(
    payload: String,
    payloadType: PayloadType,
    onCopy: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    val displayText = if (expanded || payload.length <= 300) payload
    else payload.take(300) + "…"

    val typeLabel = when (payloadType) {
        PayloadType.URL -> stringResource(R.string.payload_type_url)
        PayloadType.WIFI -> stringResource(R.string.payload_type_wifi)
        PayloadType.CONTACT -> stringResource(R.string.payload_type_contact)
        PayloadType.EMAIL -> stringResource(R.string.payload_type_email)
        PayloadType.PHONE -> stringResource(R.string.payload_type_phone)
        PayloadType.TEXT -> stringResource(R.string.payload_type_text)
    }

    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                AssistChip(onClick = {}, label = { Text(typeLabel) })
                Spacer(modifier = Modifier.width(8.dp))
                if (payloadType == PayloadType.URL) {
                    IconButton(onClick = onCopy) {
                        Icon(Icons.Filled.ContentCopy, contentDescription = stringResource(R.string.copy_link))
                    }
                }
            }
            SelectionContainer {
                Text(
                    text = displayText,
                    style = MaterialTheme.typography.bodyMedium,
                    modifier = Modifier.padding(top = 8.dp),
                )
            }
            if (payload.length > 300 && !expanded) {
                TextButton(onClick = { expanded = true }) {
                    Text(stringResource(R.string.expand))
                }
            }
        }
    }
}
