package com.vldsir.qrshield.data.network

data class ReputationResult(
    val malicious: Int,
    val suspicious: Int,
    val harmless: Int,
    val undetected: Int,
    val totalVendors: Int,
)

sealed interface ReputationOutcome {
    data class Success(val result: ReputationResult) : ReputationOutcome
    sealed interface Failure : ReputationOutcome {
        /** API key is not configured — device may be online, but lookup is impossible. */
        data object NoApiKey : Failure
        data object Offline : Failure
        data object RateLimited : Failure
        data object Timeout : Failure
        data class Http(val code: Int) : Failure
        data object Parse : Failure
    }
}
