package com.devlab74.blogx

import android.app.Application
import com.devlab74.blogx.di.AppComponent
import com.devlab74.blogx.di.DaggerAppComponent
import com.devlab74.blogx.di.auth.AuthComponent
import com.devlab74.blogx.di.main.MainComponent
import kotlinx.coroutines.FlowPreview
import timber.log.Timber

/**
 * Entry Point of the App
 *
 * Responsible for initializing Dagger components and releasing them
 */

class BaseApplication: Application() {
    @FlowPreview
    lateinit var appComponent: AppComponent

    @FlowPreview
    private var authComponent: AuthComponent? = null

    @FlowPreview
    private var mainComponent: MainComponent? = null

    @FlowPreview
    override fun onCreate() {
        super.onCreate()
        initAppComponent()

        if (BuildConfig.DEBUG) {
            Timber.plant(Timber.DebugTree())
        }
    }

    @FlowPreview
    fun initAppComponent() {
        appComponent = DaggerAppComponent.builder()
            .application(this)
            .build()
    }

    @FlowPreview
    fun authComponent(): AuthComponent {
        if (authComponent == null) {
            authComponent = appComponent.authComponent().create()
        }
        return authComponent as AuthComponent
    }

    @FlowPreview
    fun mainComponent(): MainComponent {
        if (mainComponent == null) {
            mainComponent = appComponent.mainComponent().create()
        }
        return mainComponent as MainComponent
    }

    @FlowPreview
    fun releaseAuthComponent() {
        authComponent = null
    }

    @FlowPreview
    fun releaseMainComponent() {
        mainComponent = null
    }
}