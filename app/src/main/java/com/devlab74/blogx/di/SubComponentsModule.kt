package com.devlab74.blogx.di

import com.devlab74.blogx.di.auth.AuthComponent
import com.devlab74.blogx.di.main.MainComponent
import dagger.Module
import kotlinx.coroutines.FlowPreview

/**
 * This class holding instances for sub components
 */

@FlowPreview
@Module(
    subcomponents = [
        AuthComponent::class,
        MainComponent::class
    ]
)
class SubComponentsModule