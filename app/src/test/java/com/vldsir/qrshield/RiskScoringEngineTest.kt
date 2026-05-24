package com.vldsir.qrshield

import com.vldsir.qrshield.analysis.risk.RiskScoringEngine
import com.vldsir.qrshield.analysis.risk.RiskSignal
import com.vldsir.qrshield.analysis.risk.Verdict
import com.vldsir.qrshield.analysis.wifi.WifiEncryption
import com.vldsir.qrshield.classifier.PayloadType
import org.junit.Assert.assertEquals
import org.junit.Test

class RiskScoringEngineTest {
    private val engine = RiskScoringEngine()

    @Test fun noSignalsIsSafe() {
        val (verdict, _) = engine.aggregate(emptyList(), PayloadType.URL)
        assertEquals(Verdict.SAFE, verdict)
    }

    @Test fun criticalSignalIsDangerous() {
        val signals = listOf(RiskSignal.Injection(RiskSignal.Severity.CRITICAL, "XSS", "script"))
        val (verdict, _) = engine.aggregate(signals, PayloadType.URL)
        assertEquals(Verdict.DANGEROUS, verdict)
    }

    @Test fun highSignalIsDangerous() {
        val signals = listOf(RiskSignal.Wifi(RiskSignal.Severity.HIGH, "Open", WifiEncryption.OPEN))
        val (verdict, _) = engine.aggregate(signals, PayloadType.WIFI)
        assertEquals(Verdict.DANGEROUS, verdict)
    }

    @Test fun mediumSignalIsSuspicious() {
        val signals = listOf(RiskSignal.Heuristic(RiskSignal.Severity.MEDIUM, "Suspicious TLD", 10))
        val (verdict, _) = engine.aggregate(signals, PayloadType.URL)
        assertEquals(Verdict.SUSPICIOUS, verdict)
    }

    @Test fun heuristicScoreAbove60IsDangerous() {
        val signals = listOf(
            RiskSignal.Heuristic(RiskSignal.Severity.HIGH, "Levenshtein", 35),
            RiskSignal.Heuristic(RiskSignal.Severity.HIGH, "Homoglyph", 30),
        )
        // aggregate computes score from heuristic signals, but HIGH severity also triggers DANGEROUS
        val (verdict, _) = engine.aggregate(signals, PayloadType.URL)
        assertEquals(Verdict.DANGEROUS, verdict)
    }

    @Test fun heuristicScore25to59IsSuspicious() {
        val signals = listOf(
            RiskSignal.Heuristic(RiskSignal.Severity.LOW, "Suspicious TLD", 10),
            RiskSignal.Heuristic(RiskSignal.Severity.LOW, "Long host", 5),
            RiskSignal.Heuristic(RiskSignal.Severity.LOW, "Non-HTTPS", 10),
        )
        // score = 25, no HIGH/CRITICAL/MEDIUM → suspicious from score
        val (verdict, _) = engine.aggregate(signals, PayloadType.URL)
        assertEquals(Verdict.SUSPICIOUS, verdict)
    }

    @Test fun offlineFallbackNeverRaisesSeverity() {
        val signals = listOf(RiskSignal.OfflineFallback)
        val (verdict, explanation) = engine.aggregate(signals, PayloadType.URL)
        assertEquals(Verdict.SAFE, verdict)
        assertEquals(1, explanation.bullets.size)
    }

    @Test fun bulletsAreSortedBySeverity() {
        val signals = listOf(
            RiskSignal.Heuristic(RiskSignal.Severity.LOW, "Low signal", 5),
            RiskSignal.Heuristic(RiskSignal.Severity.HIGH, "High signal", 20),
            RiskSignal.Heuristic(RiskSignal.Severity.MEDIUM, "Medium signal", 10),
        )
        val (_, explanation) = engine.aggregate(signals, PayloadType.URL)
        val bullets = explanation.bullets
        assertEquals("High signal", bullets[0])
        assertEquals("Medium signal", bullets[1])
        assertEquals("Low signal", bullets[2])
    }
}
