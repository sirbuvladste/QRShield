package com.vldsir.qrshield.analysis.url

/** Iterative two-row Damerau-Levenshtein distance. O(n·m) time, O(min(n,m)) space. */
fun levenshteinDistance(a: String, b: String): Int {
    if (a == b) return 0
    if (a.isEmpty()) return b.length
    if (b.isEmpty()) return a.length

    val (shorter, longer) = if (a.length <= b.length) a to b else b to a
    var prev = IntArray(shorter.length + 1) { it }
    var curr = IntArray(shorter.length + 1)

    for (i in 1..longer.length) {
        curr[0] = i
        for (j in 1..shorter.length) {
            val cost = if (longer[i - 1] == shorter[j - 1]) 0 else 1
            curr[j] = minOf(
                prev[j] + 1,
                curr[j - 1] + 1,
                prev[j - 1] + cost,
            )
        }
        val tmp = prev; prev = curr; curr = tmp
    }
    return prev[shorter.length]
}
