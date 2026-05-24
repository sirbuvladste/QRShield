package com.vldsir.qrshield.scanner

/** Suppresses duplicate QR detections within a 2-second window. */
class ScanDebouncer {
    private var lastPayload: String? = null
    private var lastEpochMs: Long = 0L

    fun accept(payload: String): Boolean {
        val now = System.currentTimeMillis()
        if (payload == lastPayload && now - lastEpochMs < 2000L) return false
        lastPayload = payload
        lastEpochMs = now
        return true
    }

    fun reset() {
        lastPayload = null
        lastEpochMs = 0L
    }
}
