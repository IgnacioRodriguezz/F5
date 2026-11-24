package com.rodriguez.f5

import android.app.Application
import com.rodriguez.f5.di.AppContainer

/**
 * Entry point of the process. Owns the dependency container so that every
 * ViewModel resolves its repository from a single place.
 */
class F5Application : Application() {

    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
