package com.vldsir.qrshield.analysis.injection

import com.vldsir.qrshield.analysis.risk.RiskSignal

data class InjectionFinding(
    val signal: RiskSignal.Injection,
)
