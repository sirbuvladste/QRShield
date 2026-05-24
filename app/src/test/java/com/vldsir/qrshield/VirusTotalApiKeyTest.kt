package com.vldsir.qrshield

import com.vldsir.qrshield.data.network.VirusTotalApiFactory
import kotlinx.coroutines.runBlocking
import org.junit.Assert.fail
import org.junit.Assume.assumeTrue
import org.junit.Test
import java.io.File
import java.util.Properties

/**
 * Smoke-tests the VirusTotal API key end-to-end from the host JVM.
 *
 * No device/emulator required — VirusTotalApiFactory uses only OkHttp + Retrofit,
 * which are pure JVM libraries.
 *
 * Run with:  ./gradlew :app:testDebugUnitTest --tests "*.VirusTotalApiKeyTest"
 */
class VirusTotalApiKeyTest {

    // ── helpers ────────────────────────────────────────────────────────────────

    /**
     * Reads VIRUSTOTAL_API_KEY from secrets.properties at the project root.
     * Gradle runs unit tests with cwd = <module-root> (the app/ directory),
     * so we go one level up to reach the project root.
     */
    private fun loadApiKey(): String {
        val candidates = listOf(
            File("../secrets.properties"),   // normal Gradle run (cwd = app/)
            File("secrets.properties"),      // safety fallback
        )
        val propsFile = candidates.firstOrNull { it.exists() }
            ?: return ""
        val props = Properties().also { it.load(propsFile.inputStream()) }
        return props.getProperty("VIRUSTOTAL_API_KEY", "").trim()
    }

    /** Mirrors the Base64 encoding in ReputationRepository using java.util.Base64 (JVM-only). */
    private fun urlToId(url: String): String =
        java.util.Base64.getUrlEncoder().withoutPadding()
            .encodeToString(url.toByteArray(Charsets.UTF_8))

    // ── tests ──────────────────────────────────────────────────────────────────

    @Test
    fun `api key is present in secrets_properties`() {
        val key = loadApiKey()
        if (key.isEmpty()) {
            fail(
                "\nVIRUSTOTAL_API_KEY is empty in secrets.properties.\n" +
                "Get your key at: https://www.virustotal.com/gui/my-apikey\n" +
                "Then add it to Implementation/secrets.properties."
            )
        }
        println("✓ API key found (length=${key.length}): ${key.take(8)}…")
    }

    @Test
    fun `api key is accepted by virustotal`() {
        val key = loadApiKey()
        assumeTrue("Skipping network test — API key is empty in secrets.properties", key.isNotEmpty())

        println("Using key: ${key.take(8)}… (length=${key.length})")

        val api = VirusTotalApiFactory.create()

        // google.com is always in VirusTotal's database — a safe, predictable target.
        val testUrl = "https://www.google.com"
        val id = urlToId(testUrl)
        println("Requesting reputation for: $testUrl  (id=$id)")

        val response = runBlocking { api.getUrl(id, key) }

        println("HTTP ${response.code()}")
        if (!response.isSuccessful) {
            println("Error body: ${response.errorBody()?.string()}")
        }

        when (response.code()) {
            200 -> println("✓ Success — VirusTotal returned cached analysis for $testUrl")
            404 -> println("✓ Key accepted — URL not yet in VT database (404 is fine, auth passed)")
            401 -> fail(
                "\nHTTP 401 — API key rejected by VirusTotal.\n" +
                "• Double-check the key at https://www.virustotal.com/gui/my-apikey\n" +
                "• Make sure there are no extra spaces or newline characters in secrets.properties."
            )
            403 -> fail(
                "\nHTTP 403 — Access forbidden.\n" +
                "• Your key may not have access to the URLs endpoint (requires free account).\n" +
                "• Or the key may have been suspended."
            )
            429 -> fail(
                "\nHTTP 429 — Rate limit exceeded.\n" +
                "• Free tier: 4 requests/minute, 500/day.\n" +
                "• Wait a minute and retry."
            )
            else -> fail(
                "\nUnexpected HTTP ${response.code()} from VirusTotal.\n" +
                "Error body: ${response.errorBody()?.string()}"
            )
        }
    }

    @Test
    fun `submit and fetch works end to end`() {
        val key = loadApiKey()
        assumeTrue("Skipping network test — API key is empty", key.isNotEmpty())

        val api = VirusTotalApiFactory.create()

        // Submit a URL that definitely exists
        val testUrl = "https://www.google.com"
        println("Submitting: $testUrl")
        val submitResponse = runBlocking { api.submitUrl(testUrl, key) }
        println("Submit → HTTP ${submitResponse.code()}")

        when (submitResponse.code()) {
            200, 201 -> println("✓ Submit accepted")
            401 -> fail("HTTP 401 on submit — API key is invalid.")
            429 -> {
                println("Rate limited on submit — skipping fetch check (key works)")
                return
            }
            else -> fail("Submit returned HTTP ${submitResponse.code()}: ${submitResponse.errorBody()?.string()}")
        }

        // Now fetch the result
        val id = urlToId(testUrl)
        val fetchResponse = runBlocking { api.getUrl(id, key) }
        println("Fetch → HTTP ${fetchResponse.code()}")

        if (fetchResponse.code() !in listOf(200, 404)) {
            fail("Fetch returned HTTP ${fetchResponse.code()}: ${fetchResponse.errorBody()?.string()}")
        }

        println("✓ Full submit→fetch round-trip completed successfully")
    }
}
