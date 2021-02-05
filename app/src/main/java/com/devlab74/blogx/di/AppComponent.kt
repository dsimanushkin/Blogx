package com.devlab74.blogx.di

import android.app.Application
import com.devlab74.blogx.di.auth.AuthComponent
import com.devlab74.blogx.di.main.MainComponent
import com.devlab74.blogx.session.SessionManager
import com.devlab74.blogx.ui.BaseActivity
import dagger.BindsInstance
import dagger.Component
import javax.inject.Singleton

@Singleton
@Component(
    modules = [
        AppModule::class,
        SubComponentsModule::class
    ]
)
interface AppComponent {
    val sessionManager: SessionManager

    fun inject(baseActivity: BaseActivity)

    @Component.Builder
    interface Builder{
        @BindsInstance
        fun application(application: Application): Builder

        fun build(): AppComponent
    }

    fun authComponent(): AuthComponent.Factory

    fun mainComponent(): MainComponent.Factory
}