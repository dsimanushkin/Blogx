package com.devlab74.blogx.repository.auth

import com.devlab74.blogx.di.auth.AuthScope
import com.devlab74.blogx.ui.auth.state.AuthViewState
import com.devlab74.blogx.util.DataState
import com.devlab74.blogx.util.StateEvent
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow

/**
 * This interface declaring functions that is responsible for API calls of Auth part of the App
 */

@FlowPreview
@AuthScope
interface AuthRepository {

    // Login Attempt
    fun attemptLogin(
        stateEvent: StateEvent,
        username: String,
        password: String
    ): Flow<DataState<AuthViewState>>

    // Registration Attempt
    fun attemptRegistration(
        stateEvent: StateEvent,
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): Flow<DataState<AuthViewState>>

    // Checking for previously authenticated user
    fun checkPreviousAuthUser(
        stateEvent: StateEvent
    ): Flow<DataState<AuthViewState>>

    // Saving just authenticated user to SharedPrefs
    fun saveAuthenticatedUserToPrefs(username: String)

    // In case if not auth token found this method will be called
    fun returnNoTokenFound(
        stateEvent: StateEvent
    ): DataState<AuthViewState>

}