package com.vldsir.qrshield.data.history

import com.vldsir.qrshield.analysis.risk.Verdict
import com.vldsir.qrshield.analysis.risk.VerdictExplanation
import com.vldsir.qrshield.classifier.PayloadType

data class ScanRecord(
    val id: Long,
    val timestamp: Long,
    val payload: String,
    val payloadType: PayloadType,
    val verdict: Verdict,
    val explanation: VerdictExplanation,
    val onlineLookupUsed: Boolean,
)
