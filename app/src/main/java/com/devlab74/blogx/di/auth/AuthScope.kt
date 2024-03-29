package com.devlab74.blogx.di.auth

import javax.inject.Scope

/**
 * AuthScope is strictly for login and registration parts of the application
 */

@Scope
@Retention(AnnotationRetention.RUNTIME)
annotation class AuthScope