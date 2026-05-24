package com.vldsir.qrshield.analysis.risk

import com.vldsir.qrshield.classifier.PayloadType

/** Aggregates risk signals from all analyzers into a final verdict and explanation. */
class RiskScoringEngine {

    fun aggregate(signals: List<RiskSignal>, type: PayloadType): Pair<Verdict, VerdictExplanation> {
        val heuristicScore = signals
            .filterIsInstance<RiskSignal.Heuristic>()
            .sumOf { it.score }
            .coerceAtMost(100)

        val verdict = when {
            signals.any { it.severity == RiskSignal.Severity.CRITICAL } -> Verdict.DANGEROUS
            signals.any { it.severity == RiskSignal.Severity.HIGH } -> Verdict.DANGEROUS
            heuristicScore >= 60 -> Verdict.DANGEROUS
            heuristicScore in 25..59 -> Verdict.SUSPICIOUS
            signals.any { it.severity == RiskSignal.Severity.MEDIUM } -> Verdict.SUSPICIOUS
            else -> Verdict.SAFE
        }

        val headline = when (verdict) {
            Verdict.SAFE -> "This QR code looks safe."
            Verdict.SUSPICIOUS -> "Be careful — this URL has warning signs."
            Verdict.DANGEROUS -> "Do not open — this QR code is dangerous."
        }

        val severityOrder = mapOf(
            RiskSignal.Severity.CRITICAL to 0,
            RiskSignal.Severity.HIGH to 1,
            RiskSignal.Severity.MEDIUM to 2,
            RiskSignal.Severity.LOW to 3,
            RiskSignal.Severity.INFO to 4,
        )

        val bullets = signals
            .filter { it.severity >= RiskSignal.Severity.INFO }
            .sortedBy { severityOrder[it.severity] ?: 5 }
            .map { it.rationale }
            .distinct()

        return verdict to VerdictExplanation(headline = headline, bullets = bullets)
    }
}
