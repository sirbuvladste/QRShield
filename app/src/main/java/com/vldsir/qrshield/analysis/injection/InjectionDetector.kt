package com.vldsir.qrshield.analysis.injection

import android.net.Uri
import com.vldsir.qrshield.analysis.risk.RiskSignal
import com.vldsir.qrshield.analysis.risk.RiskSignal.Severity
import com.vldsir.qrshield.util.Base64Utils

/** Detects XSS, script injection, and open-redirect patterns in a payload string. */
class InjectionDetector {

    private val scriptTag = Regex("<script\\b[^>]*>", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
    private val javascriptScheme = Regex("javascript:", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
    private val eventHandler = Regex("\\bon\\w+\\s*=", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
    private val evalCall = Regex("\\beval\\s*\\(", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
    private val docCookie = Regex("\\bdocument\\.cookie\\b", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
    private val dataTextHtml = Regex("\\bdata:text/html", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
    private val encodedScript = Regex("%3Cscript", setOf(RegexOption.IGNORE_CASE, RegexOption.MULTILINE))
    private val base64Blob = Regex("[A-Za-z0-9+/]{40,}={0,2}")

    private val redirectParams = setOf("url", "next", "redirect", "dest", "return")

    fun detect(payload: String): List<InjectionFinding> {
        val findings = mutableListOf<InjectionFinding>()

        if (scriptTag.containsMatchIn(payload)) findings += finding(Severity.HIGH, "<script> tag detected", "<script>")
        if (javascriptScheme.containsMatchIn(payload)) findings += finding(Severity.HIGH, "javascript: URI scheme detected", "javascript:")
        if (eventHandler.containsMatchIn(payload)) findings += finding(Severity.MEDIUM, "Inline event handler attribute detected", "on*=")
        if (evalCall.containsMatchIn(payload)) findings += finding(Severity.HIGH, "eval() call detected", "eval()")
        if (docCookie.containsMatchIn(payload)) findings += finding(Severity.HIGH, "document.cookie access detected", "document.cookie")
        if (dataTextHtml.containsMatchIn(payload)) findings += finding(Severity.HIGH, "data:text/html URI detected", "data:text/html")
        if (encodedScript.containsMatchIn(payload)) findings += finding(Severity.MEDIUM, "URL-encoded <script> tag detected", "%3Cscript")

        // Base64 blob — decode and re-check
        base64Blob.findAll(payload).forEach { match ->
            val decoded = Base64Utils.decodeOrNull(match.value)
            if (decoded != null) {
                val decodedStr = String(decoded, Charsets.UTF_8)
                if (containsInjectionPattern(decodedStr)) {
                    findings += finding(Severity.CRITICAL, "Base64-encoded injection payload detected", "base64-injection")
                }
            }
        }

        // Open-redirect
        detectOpenRedirect(payload)?.let { findings += it }

        return findings
    }

    private fun containsInjectionPattern(s: String): Boolean =
        scriptTag.containsMatchIn(s) ||
                javascriptScheme.containsMatchIn(s) ||
                eventHandler.containsMatchIn(s) ||
                evalCall.containsMatchIn(s) ||
                docCookie.containsMatchIn(s) ||
                dataTextHtml.containsMatchIn(s)

    private fun detectOpenRedirect(payload: String): InjectionFinding? {
        val uri = try { Uri.parse(payload) } catch (_: Exception) { return null }
        if (uri.host == null) return null
        for (param in redirectParams) {
            val value = uri.getQueryParameter(param) ?: continue
            if (value.startsWith("http://", ignoreCase = true) ||
                value.startsWith("https://", ignoreCase = true)
            ) {
                val valueHost = try { Uri.parse(value).host } catch (_: Exception) { null }
                if (valueHost != null && valueHost != uri.host) {
                    return finding(
                        Severity.HIGH,
                        "Open-redirect parameter '$param' points to a different domain",
                        "open-redirect:$param",
                    )
                }
            }
        }
        return null
    }

    private fun finding(severity: Severity, rationale: String, pattern: String) =
        InjectionFinding(RiskSignal.Injection(severity, rationale, pattern))
}
