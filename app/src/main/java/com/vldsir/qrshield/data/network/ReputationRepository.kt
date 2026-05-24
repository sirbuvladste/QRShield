package com.vldsir.qrshield.data.network

import android.util.Base64
import com.vldsir.qrshield.util.Logger
import kotlinx.coroutines.delay
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

class ReputationRepository(
    private val api: VirusTotalApi,
    private val apiKey: String,
) {

    private val tag = "ReputationRepository"

    /** Fetches URL reputation from VirusTotal. Submits if not previously seen (404). */
    suspend fun fetchReputation(url: String): ReputationOutcome {
        if (apiKey.isEmpty()) return ReputationOutcome.Failure.NoApiKey

        val id = urlToId(url)
        return try {
            val outcome = getWithRetry(id)
            if (outcome is ReputationOutcome.Failure.Http && (outcome as ReputationOutcome.Failure.Http).code == 404) {
                submitAndFetch(url, id)
            } else {
                outcome
            }
        } catch (e: SocketTimeoutException) {
            // One retry with exponential backoff
            Logger.w(tag, "Timeout on first attempt, retrying")
            delay(800)
            try {
                getWithRetry(id)
            } catch (e2: SocketTimeoutException) {
                ReputationOutcome.Failure.Timeout
            }
        } catch (e: IOException) {
            Logger.w(tag, "Network error", e)
            ReputationOutcome.Failure.Offline
        } catch (e: Exception) {
            Logger.e(tag, "Unexpected error", e)
            ReputationOutcome.Failure.Parse
        }
    }

    private suspend fun getWithRetry(id: String): ReputationOutcome {
        val resp = api.getUrl(id, apiKey)
        return when {
            resp.code() == 429 -> ReputationOutcome.Failure.RateLimited
            resp.isSuccessful -> parseResponse(resp.body())
            else -> ReputationOutcome.Failure.Http(resp.code())
        }
    }

    private suspend fun submitAndFetch(url: String, id: String): ReputationOutcome {
        return try {
            api.submitUrl(url, apiKey)
            delay(500)
            getWithRetry(id)
        } catch (_: Exception) {
            ReputationOutcome.Failure.Parse
        }
    }

    private fun parseResponse(body: VirusTotalUrlResponse?): ReputationOutcome {
        val stats = body?.data?.attributes?.lastAnalysisStats
            ?: return ReputationOutcome.Failure.Parse
        val total = stats.malicious + stats.suspicious + stats.harmless + stats.undetected
        return ReputationOutcome.Success(
            ReputationResult(
                malicious = stats.malicious,
                suspicious = stats.suspicious,
                harmless = stats.harmless,
                undetected = stats.undetected,
                totalVendors = total,
            )
        )
    }

    private fun urlToId(url: String): String =
        Base64.encodeToString(url.toByteArray(Charsets.UTF_8), Base64.URL_SAFE or Base64.NO_WRAP)
            .trimEnd('=')
}
