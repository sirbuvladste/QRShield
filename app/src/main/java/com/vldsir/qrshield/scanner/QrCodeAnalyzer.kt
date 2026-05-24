package com.vldsir.qrshield.scanner

import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import com.vldsir.qrshield.util.Logger
import kotlinx.coroutines.channels.BufferOverflow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow

/**
 * CameraX ImageAnalysis.Analyzer that decodes QR codes using ML Kit and
 * emits raw payload strings to [payloads].
 */
class QrCodeAnalyzer : ImageAnalysis.Analyzer {

    private val tag = "QrCodeAnalyzer"

    private val _payloads = MutableSharedFlow<String>(
        extraBufferCapacity = 1,
        onBufferOverflow = BufferOverflow.DROP_OLDEST,
    )
    val payloads: SharedFlow<String> = _payloads

    @Volatile
    var paused: Boolean = false

    private val scanner = BarcodeScanning.getClient(
        com.google.mlkit.vision.barcode.BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    override fun analyze(imageProxy: ImageProxy) {
        if (paused) {
            imageProxy.close()
            return
        }

        val mediaImage = imageProxy.image
        if (mediaImage == null) {
            imageProxy.close()
            return
        }

        val inputImage = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
        scanner.process(inputImage)
            .addOnSuccessListener { barcodes ->
                barcodes.firstOrNull()?.rawValue?.let { raw ->
                    _payloads.tryEmit(raw)
                }
            }
            .addOnFailureListener { e ->
                Logger.w(tag, "Barcode scan failed", e)
            }
            .addOnCompleteListener {
                imageProxy.close()
            }
    }
}
