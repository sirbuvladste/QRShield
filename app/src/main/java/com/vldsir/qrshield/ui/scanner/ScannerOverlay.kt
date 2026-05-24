package com.vldsir.qrshield.ui.scanner

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.vldsir.qrshield.R

@Composable
fun ScannerOverlay(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        // graphicsLayer with offscreen compositing is required for BlendMode.Clear to work
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
        ) {
            val side = minOf(size.width, size.height) * 0.6f
            val left = (size.width - side) / 2f
            val top = (size.height - side) / 2f
            val radius = 24.dp.toPx()

            drawRect(color = Color.Black.copy(alpha = 0.55f))

            // Punch a transparent hole for the reticle
            drawRoundRect(
                color = Color.Transparent,
                topLeft = Offset(left, top),
                size = Size(side, side),
                cornerRadius = CornerRadius(radius, radius),
                blendMode = BlendMode.Clear,
            )

            // White outline around the reticle
            drawRoundRect(
                color = Color.White,
                topLeft = Offset(left, top),
                size = Size(side, side),
                cornerRadius = CornerRadius(radius, radius),
                style = Stroke(width = 3.dp.toPx()),
            )
        }

        Text(
            text = stringResource(R.string.scan_instruction),
            color = Color.White,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 120.dp),
        )
    }
}
