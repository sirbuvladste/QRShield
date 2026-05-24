package com.vldsir.qrshield.analysis.url

import com.vldsir.qrshield.analysis.risk.RiskSignal

data class UrlHeuristicResult(
    val score: Int,
    val signals: List<RiskSignal.Heuristic>,
)
