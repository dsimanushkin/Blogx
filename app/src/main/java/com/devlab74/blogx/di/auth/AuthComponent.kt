package com.devlab74.blogx.di.auth

import com.devlab74.blogx.ui.auth.AuthActivity
import dagger.Subcomponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

/**
 * This is the Aut component interface responsible for injecting into the Auth activity
 */

@FlowPreview
@AuthScope
@Subcomponent(
    modules = [
        AuthModule::class,
        AuthViewModelModule::class,
        AuthFragmentsModule::class
    ]
)
interface AuthComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(): AuthComponent
    }

    @ExperimentalCoroutinesApi
    fun inject(authActivity: AuthActivity)
}