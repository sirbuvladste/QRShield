package com.vldsir.qrshield

import com.vldsir.qrshield.analysis.url.levenshteinDistance
import org.junit.Assert.assertEquals
import org.junit.Test

class LevenshteinDistanceTest {

    @Test fun equalStrings() = assertEquals(0, levenshteinDistance("abc", "abc"))
    @Test fun emptyStrings() = assertEquals(0, levenshteinDistance("", ""))
    @Test fun emptyToNonEmpty() = assertEquals(3, levenshteinDistance("", "abc"))
    @Test fun nonEmptyToEmpty() = assertEquals(3, levenshteinDistance("abc", ""))
    @Test fun singleInsertion() = assertEquals(1, levenshteinDistance("abc", "abcd"))
    @Test fun singleDeletion() = assertEquals(1, levenshteinDistance("abcd", "abc"))
    @Test fun singleSubstitution() = assertEquals(1, levenshteinDistance("abc", "axc"))
    @Test fun paypalExample() = assertEquals(1, levenshteinDistance("paypal.com", "paypa1.com"))
    @Test fun googleExample() = assertEquals(1, levenshteinDistance("google.com", "g00gle.com"))
    @Test fun completelyDifferent() {
        val d = levenshteinDistance("abc", "xyz")
        assertEquals(3, d)
    }
}
