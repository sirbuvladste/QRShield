package com.vldsir.qrshield.data.blocklist

import android.content.Context
import com.vldsir.qrshield.util.Logger

/** Loads the bundled high-value domain list from assets once at first access. */
class DomainBlocklistLoader(private val context: Context) {

    val highValueDomains: List<HighValueDomain> by lazy {
        try {
            context.assets.open("high_value_domains.txt")
                .bufferedReader()
                .useLines { lines ->
                    lines.map { it.trim() }
                        .filter { it.isNotEmpty() && !it.startsWith("#") }
                        .map { HighValueDomain(it.lowercase()) }
                        .toList()
                }
        } catch (e: Exception) {
            Logger.e("DomainBlocklistLoader", "Failed to load high_value_domains.txt", e)
            emptyList()
        }
    }
}
