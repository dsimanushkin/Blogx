package com.devlab74.blogx.di.auth

import androidx.lifecycle.ViewModel
import com.devlab74.blogx.di.ViewModelKey
import com.devlab74.blogx.ui.auth.AuthViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class AuthViewModelModule {
    @Binds
    @IntoMap
    @ViewModelKey(AuthViewModel::class)
    abstract fun bindAuthViewModel(authViewModel: AuthViewModel): ViewModel
}