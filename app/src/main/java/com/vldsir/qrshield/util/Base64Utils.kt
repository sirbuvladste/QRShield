package com.vldsir.qrshield.util

import android.util.Base64

object Base64Utils {
    fun decodeOrNull(input: String): ByteArray? = try {
        Base64.decode(input, Base64.NO_WRAP)
    } catch (_: IllegalArgumentException) {
        null
    }
}
