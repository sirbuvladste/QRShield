package com.vldsir.qrshield.analysis.risk

import com.vldsir.qrshield.analysis.wifi.WifiEncryption

sealed interface RiskSignal {
    val severity: Severity
    val rationale: String

    enum class Severity { INFO, LOW, MEDIUM, HIGH, CRITICAL }

    data class Heuristic(
        override val severity: Severity,
        override val rationale: String,
        val score: Int,
    ) : RiskSignal

    data class Reputation(
        override val severity: Severity,
        override val rationale: String,
        val maliciousVendors: Int,
    ) : RiskSignal

    data class Wifi(
        override val severity: Severity,
        override val rationale: String,
        val encryption: WifiEncryption,
    ) : RiskSignal

    data class Injection(
        override val severity: Severity,
        override val rationale: String,
        val pattern: String,
    ) : RiskSignal

    data object OfflineFallback : RiskSignal {
        override val severity = Severity.INFO
        override val rationale = "VirusTotal lookup skipped: device offline"
    }
}
