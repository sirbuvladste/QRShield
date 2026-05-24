package com.vldsir.qrshield.classifier

import android.util.Patterns

/** Classifies a raw QR payload into its semantic type. First match wins. */
class PayloadClassifier {

    fun classify(payload: String): ClassifiedPayload {
        val type = when {
            payload.startsWith("WIFI:", ignoreCase = true) -> PayloadType.WIFI
            isUrl(payload) -> PayloadType.URL
            isEmail(payload) -> PayloadType.EMAIL
            isPhone(payload) -> PayloadType.PHONE
            isContact(payload) -> PayloadType.CONTACT
            else -> PayloadType.TEXT
        }
        return ClassifiedPayload(raw = payload, type = type)
    }

    private fun isUrl(s: String): Boolean =
        Patterns.WEB_URL.matcher(s).matches() ||
                s.startsWith("http://", ignoreCase = true) ||
                s.startsWith("https://", ignoreCase = true)

    private fun isEmail(s: String): Boolean =
        s.startsWith("mailto:", ignoreCase = true) ||
                Patterns.EMAIL_ADDRESS.matcher(s).matches()

    private fun isPhone(s: String): Boolean =
        s.startsWith("tel:", ignoreCase = true) ||
                s.matches(Regex("""^\+?[0-9 .\-()\t]{6,}$"""))

    private fun isContact(s: String): Boolean =
        s.startsWith("BEGIN:VCARD", ignoreCase = true) ||
                s.startsWith("MECARD:", ignoreCase = true)
}
