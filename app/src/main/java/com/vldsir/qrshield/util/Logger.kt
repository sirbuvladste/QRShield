package com.vldsir.qrshield.util

import android.util.Log
import com.vldsir.qrshield.BuildConfig

object Logger {
    fun d(tag: String, msg: String) { if (BuildConfig.DEBUG) Log.d(tag, msg) }
    fun i(tag: String, msg: String) { Log.i(tag, msg) }
    fun w(tag: String, msg: String, t: Throwable? = null) { Log.w(tag, msg, t) }
    fun e(tag: String, msg: String, t: Throwable? = null) { Log.e(tag, msg, t) }
}
