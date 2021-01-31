package com.devlab74.blogx.di.auth

import com.devlab74.blogx.ui.auth.ForgotPasswordFragment
import com.devlab74.blogx.ui.auth.LauncherFragment
import com.devlab74.blogx.ui.auth.LoginFragment
import com.devlab74.blogx.ui.auth.RegisterFragment
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class AuthFragmentBuildersModule {
    @ContributesAndroidInjector()
    abstract fun contributeLauncherFragment(): LauncherFragment

    @ContributesAndroidInjector()
    abstract fun contributeLoginFragment(): LoginFragment

    @ContributesAndroidInjector()
    abstract fun contributeRegisterFragment(): RegisterFragment

    @ContributesAndroidInjector()
    abstract fun contributeForgotPasswordFragment(): ForgotPasswordFragment
}