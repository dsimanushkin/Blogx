package com.devlab74.blogx.repository.main

import com.devlab74.blogx.di.main.MainScope
import com.devlab74.blogx.models.AuthToken
import com.devlab74.blogx.ui.main.account.state.AccountViewState
import com.devlab74.blogx.util.DataState
import com.devlab74.blogx.util.StateEvent
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow

/**
 * This interface declaring functions that is responsible for API calls of Main -> Account part of the App
 */

@FlowPreview
@MainScope
interface AccountRepository {
    // Getting Account Properties
    fun getAccountProperties(
        authToken: AuthToken,
        stateEvent: StateEvent
    ): Flow<DataState<AccountViewState>>

    // Saving Account Properties
    fun saveAccountProperties(
        authToken: AuthToken,
        email: String,
        username: String,
        stateEvent: StateEvent
    ): Flow<DataState<AccountViewState>>

    // Updating Password
    fun updatePassword(
        authToken: AuthToken,
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String,
        stateEvent: StateEvent
    ): Flow<DataState<AccountViewState>>

    // This function is responsible for saving updated account information to Shared Prefs
    fun saveUpdatedUserToPrefs(username: String)
}