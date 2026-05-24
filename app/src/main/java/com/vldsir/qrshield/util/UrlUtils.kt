package com.vldsir.qrshield.util

import android.net.Uri

object UrlUtils {
    /** Extracts the registrable domain (eTLD+1 approximation) from a hostname. */
    fun registrableDomain(host: String): String {
        val parts = host.split(".")
        return if (parts.size >= 2) parts.takeLast(2).joinToString(".") else host
    }

    fun parseUri(url: String): Uri? = try {
        Uri.parse(url).takeIf { it.host != null }
    } catch (_: Exception) {
        null
    }
}
