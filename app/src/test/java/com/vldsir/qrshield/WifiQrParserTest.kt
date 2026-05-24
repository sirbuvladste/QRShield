package com.vldsir.qrshield

import com.vldsir.qrshield.analysis.wifi.WifiEncryption
import com.vldsir.qrshield.analysis.wifi.WifiQrParser
import org.junit.Assert.*
import org.junit.Test

class WifiQrParserTest {
    private val parser = WifiQrParser()

    @Test fun parseWpa2() {
        val config = parser.parse("WIFI:T:WPA2;S:MyNetwork;P:secret123;H:false;;")
        assertNotNull(config)
        assertEquals("MyNetwork", config!!.ssid)
        assertEquals(WifiEncryption.WPA2, config.encryption)
        assertEquals("secret123", config.password)
        assertFalse(config.hidden)
    }

    @Test fun parseWpa3() {
        val config = parser.parse("WIFI:T:WPA3;S:SecureNet;P:p@$$;H:false;;")
        assertEquals(WifiEncryption.WPA3, config!!.encryption)
    }

    @Test fun parseOpenNetwork() {
        val config = parser.parse("WIFI:T:nopass;S:OpenWifi;;;")
        assertEquals(WifiEncryption.OPEN, config!!.encryption)
        assertNull(config.password)
    }

    @Test fun parseWep() {
        val config = parser.parse("WIFI:T:WEP;S:OldNet;P:wepkey;;")
        assertEquals(WifiEncryption.WEP, config!!.encryption)
    }

    @Test fun parseHiddenNetwork() {
        val config = parser.parse("WIFI:T:WPA;S:HiddenNet;P:pass;H:true;;")
        assertTrue(config!!.hidden)
    }

    @Test fun parseReorderedFields() {
        val config = parser.parse("WIFI:S:ReorderedNet;P:pass;T:WPA2;H:false;;")
        assertNotNull(config)
        assertEquals("ReorderedNet", config!!.ssid)
        assertEquals(WifiEncryption.WPA2, config.encryption)
    }

    @Test fun parseWithMissingPassword() {
        val config = parser.parse("WIFI:T:WPA2;S:NoPassNet;;;")
        assertNotNull(config)
        assertNull(config!!.password)
    }

    @Test fun parseMalformedReturnsNull() {
        // Not a WIFI string — but parser won't crash
        val config = parser.parse("WIFI:")
        // Either null or empty ssid is acceptable for a malformed payload
        assertTrue(config == null || config.ssid.isEmpty())
    }
}
