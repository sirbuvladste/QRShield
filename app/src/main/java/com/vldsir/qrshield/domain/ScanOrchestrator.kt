package com.vldsir.qrshield.domain

import com.vldsir.qrshield.analysis.injection.InjectionDetector
import com.vldsir.qrshield.analysis.risk.RiskScoringEngine
import com.vldsir.qrshield.analysis.risk.RiskSignal
import com.vldsir.qrshield.analysis.url.UrlHeuristicAnalyzer
import com.vldsir.qrshield.analysis.wifi.WifiAnalyzer
import com.vldsir.qrshield.analysis.wifi.WifiQrParser
import com.vldsir.qrshield.classifier.PayloadClassifier
import com.vldsir.qrshield.classifier.PayloadType
import com.vldsir.qrshield.data.history.ScanHistoryRepository
import com.vldsir.qrshield.data.history.ScanRecord
import com.vldsir.qrshield.data.network.NetworkAvailability
import com.vldsir.qrshield.data.network.ReputationOutcome
import com.vldsir.qrshield.data.network.ReputationRepository
import com.vldsir.qrshield.util.Logger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Executes the full QR analysis pipeline from raw payload to a persisted ScanRecord.
 * Returns the new row id.
 */
class ScanOrchestrator(
    private val classifier: PayloadClassifier,
    private val urlAnalyzer: UrlHeuristicAnalyzer,
    private val wifiParser: WifiQrParser,
    private val wifiAnalyzer: WifiAnalyzer,
    private val injectionDetector: InjectionDetector,
    private val reputationRepository: ReputationRepository,
    private val networkAvailability: NetworkAvailability,
    private val riskEngine: RiskScoringEngine,
    private val historyRepository: ScanHistoryRepository,
) {

    private val tag = "ScanOrchestrator"

    suspend fun analyze(raw: String): Long = withContext(Dispatchers.IO) {
        val classified = classifier.classify(raw)
        val signals = mutableListOf<RiskSignal>()
        var onlineLookupUsed = false

        when (classified.type) {
            PayloadType.URL -> {
                val heuristic = urlAnalyzer.analyze(raw)
                signals += heuristic.signals
                signals += injectionDetector.detect(raw).map { it.signal }

                if (networkAvailability.isOnline()) {
                    when (val outcome = reputationRepository.fetchReputation(raw)) {
                        is ReputationOutcome.Success -> {
                            onlineLookupUsed = true
                            val rep = outcome.result
                            signals += mapReputationSignal(rep.malicious, rep.suspicious, rep.totalVendors)
                        }
                        is ReputationOutcome.Failure.NoApiKey -> {
                            // Device is online but VirusTotal API key is not configured —
                            // this is a build-time configuration issue, not a network error.
                            Logger.i(tag, "VirusTotal lookup skipped: API key not configured")
                            signals += RiskSignal.OfflineFallback
                        }
                        else -> {
                            Logger.w(tag, "Reputation lookup failed: $outcome")
                            signals += RiskSignal.OfflineFallback
                        }
                    }
                } else {
                    signals += RiskSignal.OfflineFallback
                }
            }

            PayloadType.WIFI -> {
                val config = wifiParser.parse(raw)
                if (config != null) {
                    signals += wifiAnalyzer.analyze(config)
                    // Paranoia injection check on SSID and password
                    signals += injectionDetector.detect(config.ssid).map { it.signal }
                    config.password?.let { signals += injectionDetector.detect(it).map { f -> f.signal } }
                } else {
                    signals += RiskSignal.Heuristic(
                        severity = RiskSignal.Severity.MEDIUM,
                        rationale = "Malformed WiFi payload",
                        score = 25,
                    )
                }
            }

            else -> {
                signals += injectionDetector.detect(raw).map { it.signal }
            }
        }

        val (verdict, explanation) = riskEngine.aggregate(signals, classified.type)

        val record = ScanRecord(
            id = 0,
            timestamp = System.currentTimeMillis(),
            payload = raw,
            payloadType = classified.type,
            verdict = verdict,
            explanation = explanation,
            onlineLookupUsed = onlineLookupUsed,
        )

        historyRepository.insert(record)
    }

    private fun mapReputationSignal(malicious: Int, suspicious: Int, totalVendors: Int): RiskSignal.Reputation =
        when {
            malicious >= 3 -> RiskSignal.Reputation(
                severity = RiskSignal.Severity.HIGH,
                rationale = "Flagged as malicious by $malicious security vendors",
                maliciousVendors = malicious,
            )
            malicious in 1..2 -> RiskSignal.Reputation(
                severity = RiskSignal.Severity.MEDIUM,
                rationale = "Flagged as malicious by $malicious security vendor${if (malicious > 1) "s" else ""}",
                maliciousVendors = malicious,
            )
            suspicious >= 3 && malicious == 0 -> RiskSignal.Reputation(
                severity = RiskSignal.Severity.LOW,
                rationale = "Flagged as suspicious by $suspicious security vendors",
                maliciousVendors = 0,
            )
            else -> RiskSignal.Reputation(
                severity = RiskSignal.Severity.INFO,
                rationale = "No security vendors flagged this URL",
                maliciousVendors = 0,
            )
        }
}
