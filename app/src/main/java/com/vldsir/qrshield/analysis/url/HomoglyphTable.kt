package com.vldsir.qrshield.analysis.url

/** Maps confusable Unicode characters to their canonical Latin equivalents. */
object HomoglyphTable {

    private val table: Map<Char, Char> = mapOf(
        'а' to 'a', 'е' to 'e', 'о' to 'o', 'р' to 'p', 'с' to 'c',
        'у' to 'y', 'х' to 'x', 'і' to 'i', 'ӏ' to 'l', 'ʟ' to 'l',
        'ν' to 'v', 'ѵ' to 'v', 'ѕ' to 's', 'ⅼ' to 'l',
        '0' to 'o', '1' to 'l',
    )

    /** Returns [host] lowercased with every confusable character replaced by its canonical form. */
    fun normalise(host: String): String =
        host.lowercase().map { table[it] ?: it }.joinToString("")
}
