package com.vldsir.qrshield

import com.vldsir.qrshield.analysis.injection.InjectionDetector
import com.vldsir.qrshield.analysis.risk.RiskSignal
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class InjectionDetectorTest {
    private val detector = InjectionDetector()

    @Test fun cleanUrl() {
        val findings = detector.detect("https://example.com")
        assertTrue(findings.isEmpty())
    }

    @Test fun scriptTag() {
        val findings = detector.detect("https://example.com?q=<script>alert(1)</script>")
        assertTrue(findings.any { it.signal.pattern.contains("script") })
        assertTrue(findings.any { it.signal.severity == RiskSignal.Severity.HIGH })
    }

    @Test fun javascriptScheme() {
        val findings = detector.detect("javascript:alert(1)")
        assertTrue(findings.any { it.signal.pattern == "javascript:" })
    }

    @Test fun eventHandler() {
        val findings = detector.detect("https://example.com?a=\" onmouseover=\"alert(1)")
        assertTrue(findings.any { it.signal.pattern == "on*=" })
    }

    @Test fun evalCall() {
        val findings = detector.detect("https://example.com/page?x=eval(atob('dGVzdA=='))")
        assertTrue(findings.any { it.signal.pattern == "eval()" })
    }

    @Test fun openRedirect() {
        val findings = detector.detect("https://example.com/login?redirect=https://evil.com")
        assertTrue(findings.any { it.signal.pattern.startsWith("open-redirect") })
    }

    @Test fun noRedirectSameDomain() {
        val findings = detector.detect("https://example.com/login?redirect=https://example.com/home")
        assertTrue(findings.none { it.signal.pattern.startsWith("open-redirect") })
    }
}
