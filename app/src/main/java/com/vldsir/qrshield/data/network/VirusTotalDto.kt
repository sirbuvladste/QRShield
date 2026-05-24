package com.vldsir.qrshield.data.network

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class VirusTotalUrlResponse(
    val data: VirusTotalData? = null,
)

@Serializable
data class VirusTotalData(
    val attributes: VirusTotalAttributes? = null,
)

@Serializable
data class VirusTotalAttributes(
    @SerialName("last_analysis_stats") val lastAnalysisStats: AnalysisStats? = null,
)

@Serializable
data class AnalysisStats(
    val malicious: Int = 0,
    val suspicious: Int = 0,
    val harmless: Int = 0,
    val undetected: Int = 0,
)

@Serializable
data class VirusTotalSubmitResponse(
    val data: SubmitData? = null,
)

@Serializable
data class SubmitData(
    val id: String? = null,
)
