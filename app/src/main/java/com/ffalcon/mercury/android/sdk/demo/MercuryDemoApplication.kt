package com.ffalcon.mercury.android.sdk.demo

import android.app.Application
import com.ffalcon.mercury.android.sdk.MercurySDK

class MercuryDemoApplication : Application() {
    companion object {
        lateinit var appContext: Application
    }

    override fun onCreate() {
        super.onCreate()
        appContext = this
        MercurySDK.init(this)
    }
}