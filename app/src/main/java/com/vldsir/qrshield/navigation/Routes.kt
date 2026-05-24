package com.vldsir.qrshield.navigation

object Routes {
    const val SCANNER = "scanner"
    const val RESULT = "result/{scanId}"
    const val HISTORY = "history"
    const val SETTINGS = "settings"
    const val TUTORIAL = "tutorial"

    fun result(scanId: Long) = "result/$scanId"
}
