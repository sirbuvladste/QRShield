package com.vldsir.qrshield

import com.vldsir.qrshield.classifier.PayloadClassifier
import com.vldsir.qrshield.classifier.PayloadType
import org.junit.Assert.assertEquals
import org.junit.Test

class PayloadClassifierTest {
    private val classifier = PayloadClassifier()

    @Test fun classifiesHttpUrl() {
        assertEquals(PayloadType.URL, classifier.classify("https://example.com").type)
    }

    @Test fun classifiesHttpInsecureUrl() {
        assertEquals(PayloadType.URL, classifier.classify("http://example.com").type)
    }

    @Test fun classifiesWifi() {
        assertEquals(PayloadType.WIFI, classifier.classify("WIFI:T:WPA;S:MyNet;P:pass;;").type)
    }

    @Test fun classifiesWifiCaseInsensitive() {
        assertEquals(PayloadType.WIFI, classifier.classify("wifi:T:WPA;S:MyNet;;").type)
    }

    @Test fun classifiesEmail() {
        assertEquals(PayloadType.EMAIL, classifier.classify("mailto:test@example.com").type)
    }

    @Test fun classifiesEmailAddress() {
        assertEquals(PayloadType.EMAIL, classifier.classify("user@example.com").type)
    }

    @Test fun classifiesPhone() {
        assertEquals(PayloadType.PHONE, classifier.classify("tel:+40123456789").type)
    }

    @Test fun classifiesPhoneNumber() {
        assertEquals(PayloadType.PHONE, classifier.classify("+40 123 456 789").type)
    }

    @Test fun classifiesVcard() {
        assertEquals(PayloadType.CONTACT, classifier.classify("BEGIN:VCARD\nFN:John Doe\nEND:VCARD").type)
    }

    @Test fun classifiesMecard() {
        assertEquals(PayloadType.CONTACT, classifier.classify("MECARD:N:John;").type)
    }

    @Test fun classifiesTextFallback() {
        assertEquals(PayloadType.TEXT, classifier.classify("Hello World").type)
    }

    @Test fun classifiesEmptyAsText() {
        assertEquals(PayloadType.TEXT, classifier.classify("").type)
    }

    @Test fun wifiHasPriorityOverUrl() {
        // WIFI prefix beats URL detection
        assertEquals(PayloadType.WIFI, classifier.classify("WIFI:T:WPA;S:https://net;;").type)
    }
}
