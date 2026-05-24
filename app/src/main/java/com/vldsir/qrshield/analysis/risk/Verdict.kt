package com.vldsir.qrshield.analysis.risk

enum class Verdict { SAFE, SUSPICIOUS, DANGEROUS }

data class VerdictExplanation(
    val headline: String,
    val bullets: List<String>,
)
