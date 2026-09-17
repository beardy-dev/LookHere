package com.beardydev.lookhere

import android.app.Application
import com.beardydev.lookhere.di.AppContainer

class LookHereApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
