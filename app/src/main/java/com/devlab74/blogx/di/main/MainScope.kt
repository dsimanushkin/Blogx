package com.devlab74.blogx.di.main

import javax.inject.Scope

/**
 * MainScope is strictly for other parts of the app excluding login and registration
 */

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class MainScope