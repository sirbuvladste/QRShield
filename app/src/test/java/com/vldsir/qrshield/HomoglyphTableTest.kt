package com.vldsir.qrshield

import com.vldsir.qrshield.analysis.url.HomoglyphTable
import org.junit.Assert.assertEquals
import org.junit.Test

class HomoglyphTableTest {

    @Test fun cyrillicA() = assertEquals("a", HomoglyphTable.normalise("а"))
    @Test fun cyrillicE() = assertEquals("e", HomoglyphTable.normalise("е"))
    @Test fun cyrillicO() = assertEquals("o", HomoglyphTable.normalise("о"))
    @Test fun cyrillicP() = assertEquals("p", HomoglyphTable.normalise("р"))
    @Test fun cyrillicC() = assertEquals("c", HomoglyphTable.normalise("с"))
    @Test fun cyrillicY() = assertEquals("y", HomoglyphTable.normalise("у"))
    @Test fun cyrillicX() = assertEquals("x", HomoglyphTable.normalise("х"))
    @Test fun cyrillicI() = assertEquals("i", HomoglyphTable.normalise("і"))
    @Test fun zeroToO() = assertEquals("o", HomoglyphTable.normalise("0"))
    @Test fun oneToL() = assertEquals("l", HomoglyphTable.normalise("1"))
    @Test fun noSubstitution() = assertEquals("hello", HomoglyphTable.normalise("hello"))
    @Test fun mixedCase() = assertEquals("paypal", HomoglyphTable.normalise("PAYPAL"))
    @Test fun realAttack() = assertEquals("paypal.com", HomoglyphTable.normalise("pаypal.com")) // 'а' is Cyrillic
}
