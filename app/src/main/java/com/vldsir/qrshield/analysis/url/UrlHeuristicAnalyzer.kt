package com.vldsir.qrshield.analysis.url

import android.net.Uri
import com.vldsir.qrshield.analysis.risk.RiskSignal
import com.vldsir.qrshield.analysis.risk.RiskSignal.Severity
import com.vldsir.qrshield.data.blocklist.DomainBlocklistLoader
import com.vldsir.qrshield.data.blocklist.HighValueDomain
import com.vldsir.qrshield.util.UrlUtils

/**
 * Computes a heuristic risk score for a URL using the bundled high-value
 * domain list. Pure function — safe to call from any dispatcher.
 *
 * @return [UrlHeuristicResult] with cumulative score (0..100) and per-check signals.
 */
class UrlHeuristicAnalyzer(private val blocklistLoader: DomainBlocklistLoader) {

    private val suspiciousTlds = setOf("zip", "mov", "xyz", "top", "gq", "tk", "ml", "cf", "ga")
    private val ipv4Regex = Regex("""^\d{1,3}(\.\d{1,3}){3}$""")
    private val ipv6Regex = Regex("""^\[?[0-9a-fA-F:]+]?$""")

    fun analyze(url: String): UrlHeuristicResult {
        val signals = mutableListOf<RiskSignal.Heuristic>()
        val uri = try { Uri.parse(url) } catch (_: Exception) { return UrlHeuristicResult(0, emptyList()) }
        val host = uri.host?.lowercase() ?: return UrlHeuristicResult(0, emptyList())
        val scheme = uri.scheme?.lowercase() ?: ""
        val highValueDomains = blocklistLoader.highValueDomains

        // Non-HTTPS scheme: weight 10
        if (scheme != "https") {
            signals += signal(10, Severity.LOW, "Non-HTTPS scheme: connection is unencrypted")
        }

        // Punycode / IDN: weight 15
        // Legitimate IDN, but also the standard vehicle for homoglyph attacks.
        if (host.contains("xn--")) {
            signals += signal(15, Severity.MEDIUM, "Punycode/IDN domain — possible homoglyph attack")
        }

        // Homoglyph substitution: weight 30
        val normalisedHost = HomoglyphTable.normalise(host)
        val normalisedRegistrable = UrlUtils.registrableDomain(normalisedHost)
        highValueDomains.forEach { hv ->
            val hvNorm = HomoglyphTable.normalise(hv.domain)
            if (normalisedRegistrable == hvNorm && UrlUtils.registrableDomain(host) != hv.domain) {
                signals += signal(30, Severity.HIGH, "Homoglyph substitution mimicking '${hv.domain}'")
            }
        }

        // Levenshtein distance ≤ 2 vs high-value domain (and not exact): weight 35
        val registrable = UrlUtils.registrableDomain(host)
        highValueDomains.forEach { hv ->
            if (registrable != hv.domain) {
                val dist = levenshteinDistance(registrable, hv.domain)
                if (dist in 1..2) {
                    signals += signal(35, Severity.HIGH, "Domain '$registrable' is suspiciously similar to '${hv.domain}' (edit distance $dist)")
                }
            }
        }

        // Suspicious TLD: weight 10
        val tld = host.substringAfterLast('.')
        if (tld in suspiciousTlds) {
            signals += signal(10, Severity.MEDIUM, "Suspicious TLD '.$tld' commonly used for abuse")
        }

        // Misleading subdomain: weight 15
        // A high-value domain appears as a subdomain of an unrelated registrable domain
        highValueDomains.forEach { hv ->
            if (host.contains("${hv.domain}.") && UrlUtils.registrableDomain(host) != hv.domain) {
                signals += signal(15, Severity.HIGH, "High-value domain '${hv.domain}' used as subdomain of an unrelated host")
            }
        }

        // Long host or many subdomains: weight 5
        val subdomainCount = host.count { it == '.' }
        if (host.length > 50 || subdomainCount > 4) {
            signals += signal(5, Severity.LOW, "Unusually long host or many subdomains")
        }

        // IP address as host: weight 20
        if (isIpAddress(host)) {
            signals += signal(20, Severity.HIGH, "IP address used as host instead of a domain name")
        }

        // @ user-info in URL: weight 15
        if (url.contains('@')) {
            val atIndex = url.indexOf('@')
            val schemeEnd = url.indexOf("://")
            if (schemeEnd == -1 || atIndex < url.indexOf('/', schemeEnd + 3).let { if (it == -1) url.length else it }) {
                signals += signal(15, Severity.HIGH, "URL contains '@' — user-info trick to obscure real destination")
            }
        }

        // Excessive URL-encoding: weight 5
        val percentCount = url.count { it == '%' }
        if (percentCount > 8) {
            signals += signal(5, Severity.LOW, "Excessive URL-encoding ($percentCount percent-encoded characters)")
        }

        val totalScore = signals.sumOf { it.score }.coerceAtMost(100)
        return UrlHeuristicResult(score = totalScore, signals = signals)
    }

    private fun signal(score: Int, severity: Severity, rationale: String) =
        RiskSignal.Heuristic(severity = severity, rationale = rationale, score = score)

    private fun isIpAddress(host: String): Boolean =
        ipv4Regex.matches(host) || ipv6Regex.matches(host.trim('[', ']'))
}
