package com.devlab74.blogx.di

import com.devlab74.blogx.di.auth.AuthComponent
import com.devlab74.blogx.di.main.MainComponent
import dagger.Module
import kotlinx.coroutines.FlowPreview

@FlowPreview
@Module(
    subcomponents = [
        AuthComponent::class,
        MainComponent::class
    ]
)
class SubComponentsModule