package com.vldsir.qrshield.ui.scanner

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.common.InputImage
import com.vldsir.qrshield.data.network.NetworkAvailability
import com.vldsir.qrshield.domain.ScanOrchestrator
import com.vldsir.qrshield.scanner.QrCodeAnalyzer
import com.vldsir.qrshield.scanner.ScanDebouncer
import com.vldsir.qrshield.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

data class ScannerUiState(
    val cameraPermissionGranted: Boolean = false,
    val analyzing: Boolean = false,
    val errorMessage: String? = null,
    val isOnline: Boolean = false,
)

sealed interface ScannerNavEvent {
    data class NavigateToResult(val scanId: Long) : ScannerNavEvent
}

class ScannerViewModel(
    private val orchestrator: ScanOrchestrator,
    private val networkAvailability: NetworkAvailability,
) : ViewModel() {

    private val tag = "ScannerViewModel"
    private val debouncer = ScanDebouncer()

    val analyzer = QrCodeAnalyzer()

    private val _uiState = MutableStateFlow(ScannerUiState())
    val uiState: StateFlow<ScannerUiState> = _uiState.asStateFlow()

    private val _navEvents = MutableSharedFlow<ScannerNavEvent>()
    val navEvents: SharedFlow<ScannerNavEvent> = _navEvents.asSharedFlow()

    /** Listens for real-time connectivity changes to keep [ScannerUiState.isOnline] current. */
    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) = updateOnlineStatus()
        override fun onLost(network: Network) = updateOnlineStatus()
        override fun onCapabilitiesChanged(network: Network, caps: NetworkCapabilities) = updateOnlineStatus()
    }

    init {
        updateOnlineStatus()
        networkAvailability.addCallback(networkCallback)
    }

    private fun updateOnlineStatus() {
        _uiState.update { it.copy(isOnline = networkAvailability.isOnline()) }
    }

    /** Scanner instance reused for gallery image scanning. */
    private val galleryScanner = BarcodeScanning.getClient(
        BarcodeScannerOptions.Builder()
            .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
            .build()
    )

    fun onPermissionResult(granted: Boolean) {
        _uiState.update { it.copy(cameraPermissionGranted = granted) }
    }

    /** Called when the live camera analyzer emits a raw QR payload. */
    fun onPayloadDetected(raw: String) {
        if (_uiState.value.analyzing) return
        if (!debouncer.accept(raw)) return

        _uiState.update { it.copy(analyzing = true) }
        analyzer.paused = true

        viewModelScope.launch {
            try {
                val id = orchestrator.analyze(raw)
                _navEvents.emit(ScannerNavEvent.NavigateToResult(id))
            } catch (e: Exception) {
                Logger.e(tag, "Analysis failed", e)
                _uiState.update { it.copy(analyzing = false, errorMessage = "Could not analyze QR code.") }
                analyzer.paused = false
            }
        }
    }

    /**
     * Scans a gallery image URI for a QR code using ML Kit, then runs the full
     * analysis pipeline if a code is found.
     */
    fun onGalleryImagePicked(context: Context, uri: Uri) {
        if (_uiState.value.analyzing) return

        _uiState.update { it.copy(analyzing = true) }
        analyzer.paused = true

        viewModelScope.launch(Dispatchers.IO) {
            try {
                val image = InputImage.fromFilePath(context, uri)
                val barcodes = galleryScanner.process(image).await()
                val raw = barcodes.firstOrNull()?.rawValue

                if (raw != null) {
                    val id = orchestrator.analyze(raw)
                    _navEvents.emit(ScannerNavEvent.NavigateToResult(id))
                } else {
                    _uiState.update {
                        it.copy(analyzing = false, errorMessage = "No QR code found in this image.")
                    }
                    analyzer.paused = false
                }
            } catch (e: Exception) {
                Logger.e(tag, "Gallery scan failed", e)
                _uiState.update {
                    it.copy(analyzing = false, errorMessage = "Could not scan image.")
                }
                analyzer.paused = false
            }
        }
    }

    fun onErrorDismissed() {
        _uiState.update { it.copy(errorMessage = null) }
    }

    fun resumeScanning() {
        analyzer.paused = false
        debouncer.reset()
        _uiState.update { it.copy(analyzing = false) }
    }

    override fun onCleared() {
        super.onCleared()
        networkAvailability.removeCallback(networkCallback)
        galleryScanner.close()
    }
}
