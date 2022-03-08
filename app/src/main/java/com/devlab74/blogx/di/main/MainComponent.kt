package com.devlab74.blogx.di.main

import com.devlab74.blogx.ui.main.MainActivity
import dagger.Subcomponent
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

/**
 * This is the Main component interface responsible for injecting into the Main activity
 */

@FlowPreview
@MainScope
@Subcomponent(
    modules = [
        MainModule::class,
        MainViewModelModule::class,
        MainFragmentsModule::class
    ]
)
interface MainComponent {
    @Subcomponent.Factory
    interface Factory {
        fun create(): MainComponent
    }

    @ExperimentalCoroutinesApi
    fun inject(mainActivity: MainActivity)
}