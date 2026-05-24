package com.vldsir.qrshield.analysis.wifi

/**
 * Parses WIFI QR payloads in the format WIFI:T:<type>;S:<ssid>;P:<password>;H:<hidden>;;
 * Field order is not guaranteed. Handles escape sequences: \: \; \, \" \\
 */
class WifiQrParser {

    fun parse(raw: String): WifiConfig? = try {
        val stripped = raw.removePrefix("WIFI:").removePrefix("wifi:")
        val fields = tokenize(stripped)
        val typeStr = fields["T"]?.uppercase()
        val ssid = fields["S"] ?: return null
        val password = fields["P"]?.takeIf { it.isNotEmpty() }
        val hidden = fields["H"]?.lowercase() == "true"
        val encryption = when (typeStr) {
            "WPA3" -> WifiEncryption.WPA3
            "WPA2" -> WifiEncryption.WPA2
            "WPA" -> WifiEncryption.WPA
            "WEP" -> WifiEncryption.WEP
            "NOPASS", "" -> WifiEncryption.OPEN
            null -> WifiEncryption.OPEN
            else -> WifiEncryption.UNKNOWN
        }
        WifiConfig(ssid = ssid, encryption = encryption, password = password, hidden = hidden)
    } catch (_: Exception) {
        null
    }

    private fun tokenize(input: String): Map<String, String> {
        val result = mutableMapOf<String, String>()
        var i = 0
        while (i < input.length) {
            val keyBuilder = StringBuilder()
            // Read key until ':'
            while (i < input.length && input[i] != ':') {
                if (input[i] == '\\' && i + 1 < input.length) {
                    keyBuilder.append(input[i + 1])
                    i += 2
                } else {
                    keyBuilder.append(input[i++])
                }
            }
            if (i >= input.length) break
            i++ // skip ':'
            val valueBuilder = StringBuilder()
            // Read value until ';' (not escaped)
            while (i < input.length && input[i] != ';') {
                if (input[i] == '\\' && i + 1 < input.length) {
                    valueBuilder.append(input[i + 1])
                    i += 2
                } else {
                    valueBuilder.append(input[i++])
                }
            }
            if (i < input.length) i++ // skip ';'
            val key = keyBuilder.toString().trim()
            if (key.isNotEmpty()) {
                result[key] = valueBuilder.toString()
            }
        }
        return result
    }
}
