package com.devlab74.blogx.di.auth

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.devlab74.blogx.fragments.auth.AuthFragmentFactory
import dagger.Module
import dagger.Provides

/**
 * This class is responsible for injecting into Auth Fragments
 */

@Module
object AuthFragmentsModule {
    @JvmStatic
    @AuthScope
    @Provides
    fun provideFragmentFactory(
        viewModelFactory: ViewModelProvider.Factory
    ): FragmentFactory {
        return AuthFragmentFactory(
            viewModelFactory
        )
    }
}