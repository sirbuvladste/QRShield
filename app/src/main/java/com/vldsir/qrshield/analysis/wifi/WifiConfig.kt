package com.vldsir.qrshield.analysis.wifi

data class WifiConfig(
    val ssid: String,
    val encryption: WifiEncryption,
    val password: String?,
    val hidden: Boolean,
)
