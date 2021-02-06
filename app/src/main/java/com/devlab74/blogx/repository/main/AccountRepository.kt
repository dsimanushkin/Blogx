package com.devlab74.blogx.repository.main

import com.devlab74.blogx.di.main.MainScope
import com.devlab74.blogx.models.AuthToken
import com.devlab74.blogx.ui.main.account.state.AccountViewState
import com.devlab74.blogx.util.DataState
import com.devlab74.blogx.util.StateEvent
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow

@FlowPreview
@MainScope
interface AccountRepository {
    fun getAccountProperties(
        authToken: AuthToken,
        stateEvent: StateEvent
    ): Flow<DataState<AccountViewState>>

    fun saveAccountProperties(
        authToken: AuthToken,
        email: String,
        username: String,
        stateEvent: StateEvent
    ): Flow<DataState<AccountViewState>>

    fun updatePassword(
        authToken: AuthToken,
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String,
        stateEvent: StateEvent
    ): Flow<DataState<AccountViewState>>

    fun saveUpdatedUserToPrefs(username: String)
}