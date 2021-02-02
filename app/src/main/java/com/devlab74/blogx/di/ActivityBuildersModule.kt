package com.devlab74.blogx.di

import com.devlab74.blogx.di.auth.AuthFragmentBuildersModule
import com.devlab74.blogx.di.auth.AuthModule
import com.devlab74.blogx.di.auth.AuthScope
import com.devlab74.blogx.di.auth.AuthViewModelModule
import com.devlab74.blogx.di.main.MainFragmentBuildersModule
import com.devlab74.blogx.di.main.MainModule
import com.devlab74.blogx.di.main.MainScope
import com.devlab74.blogx.di.main.MainViewModelModule
import com.devlab74.blogx.ui.auth.AuthActivity
import com.devlab74.blogx.ui.main.MainActivity
import dagger.Module
import dagger.android.ContributesAndroidInjector

@Module
abstract class ActivityBuildersModule {
    @AuthScope
    @ContributesAndroidInjector(
        modules = [
            AuthModule::class,
            AuthFragmentBuildersModule::class,
            AuthViewModelModule::class
        ]
    )
    abstract fun contributeAuthActivity(): AuthActivity

    @MainScope
    @ContributesAndroidInjector(
        modules = [
            MainModule::class,
            MainFragmentBuildersModule::class,
            MainViewModelModule::class
        ]
    )
    abstract fun contributeMainActivity(): MainActivity
}