package com.vldsir.qrshield.domain

import com.vldsir.qrshield.data.history.ScanRecord

sealed interface ScanResult {
    data class Success(val record: ScanRecord) : ScanResult
    data class Failure(val cause: Throwable) : ScanResult
}
