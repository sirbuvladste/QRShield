package com.vldsir.qrshield

import com.vldsir.qrshield.analysis.risk.RiskSignal
import com.vldsir.qrshield.analysis.wifi.WifiAnalyzer
import com.vldsir.qrshield.analysis.wifi.WifiConfig
import com.vldsir.qrshield.analysis.wifi.WifiEncryption
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class WifiAnalyzerTest {
    private val analyzer = WifiAnalyzer()

    private fun config(enc: WifiEncryption, hidden: Boolean = false) =
        WifiConfig(ssid = "TestNet", encryption = enc, password = "pass", hidden = hidden)

    @Test fun openNetworkIsHigh() {
        val signals = analyzer.analyze(config(WifiEncryption.OPEN))
        assertTrue(signals.any { it.severity == RiskSignal.Severity.HIGH })
    }

    @Test fun wepIsHigh() {
        val signals = analyzer.analyze(config(WifiEncryption.WEP))
        assertTrue(signals.any { it.severity == RiskSignal.Severity.HIGH })
    }

    @Test fun unknownIsMedium() {
        val signals = analyzer.analyze(config(WifiEncryption.UNKNOWN))
        assertTrue(signals.any { it.severity == RiskSignal.Severity.MEDIUM })
    }

    @Test fun wpaIsInfo() {
        val signals = analyzer.analyze(config(WifiEncryption.WPA))
        assertTrue(signals.any { it.severity == RiskSignal.Severity.INFO })
    }

    @Test fun wpa2IsInfo() {
        val signals = analyzer.analyze(config(WifiEncryption.WPA2))
        assertTrue(signals.all {
            it.severity == RiskSignal.Severity.INFO || it.severity == RiskSignal.Severity.LOW
        })
    }

    @Test fun wpa3IsInfo() {
        val signals = analyzer.analyze(config(WifiEncryption.WPA3))
        assertTrue(signals.none {
            it.severity == RiskSignal.Severity.MEDIUM ||
            it.severity == RiskSignal.Severity.HIGH ||
            it.severity == RiskSignal.Severity.CRITICAL
        })
    }

    @Test fun hiddenFlagAddsLow() {
        val signals = analyzer.analyze(config(WifiEncryption.WPA2, hidden = true))
        assertTrue(signals.any { it.severity == RiskSignal.Severity.LOW && it.rationale.contains("Hidden") })
    }
}
