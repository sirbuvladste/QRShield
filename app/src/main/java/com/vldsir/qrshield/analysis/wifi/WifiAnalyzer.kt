package com.vldsir.qrshield.analysis.wifi

import com.vldsir.qrshield.analysis.risk.RiskSignal
import com.vldsir.qrshield.analysis.risk.RiskSignal.Severity

class WifiAnalyzer {

    fun analyze(config: WifiConfig): List<RiskSignal> {
        val signals = mutableListOf<RiskSignal>()
        when (config.encryption) {
            WifiEncryption.OPEN -> signals += RiskSignal.Wifi(
                severity = Severity.HIGH,
                rationale = "Open network — traffic is unencrypted and exposes you to MitM interception",
                encryption = WifiEncryption.OPEN,
            )
            WifiEncryption.WEP -> signals += RiskSignal.Wifi(
                severity = Severity.HIGH,
                rationale = "WEP encryption is broken and can be cracked in minutes",
                encryption = WifiEncryption.WEP,
            )
            WifiEncryption.UNKNOWN -> signals += RiskSignal.Wifi(
                severity = Severity.MEDIUM,
                rationale = "Unknown encryption type — proceed with caution",
                encryption = WifiEncryption.UNKNOWN,
            )
            WifiEncryption.WPA, WifiEncryption.WPA2, WifiEncryption.WPA3 -> signals += RiskSignal.Wifi(
                severity = Severity.INFO,
                rationale = "Modern WPA-family encryption",
                encryption = config.encryption,
            )
        }
        if (config.hidden) {
            signals += RiskSignal.Heuristic(
                severity = Severity.LOW,
                rationale = "Hidden SSID",
                score = 0,
            )
        }
        return signals
    }
}
