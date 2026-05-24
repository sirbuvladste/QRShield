package com.vldsir.qrshield

import android.app.Application
import com.vldsir.qrshield.di.AppContainer

class QRShieldApplication : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
