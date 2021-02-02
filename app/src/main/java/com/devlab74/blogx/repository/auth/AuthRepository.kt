package com.devlab74.blogx.repository.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.devlab74.blogx.api.auth.BlogxAuthService
import com.devlab74.blogx.models.AuthToken
import com.devlab74.blogx.persistence.AccountPropertiesDao
import com.devlab74.blogx.persistence.AuthTokenDao
import com.devlab74.blogx.session.SessionManager
import com.devlab74.blogx.ui.DataState
import com.devlab74.blogx.ui.Response
import com.devlab74.blogx.ui.ResponseType
import com.devlab74.blogx.ui.auth.state.AuthViewState
import com.devlab74.blogx.util.ApiEmptyResponse
import com.devlab74.blogx.util.ApiErrorResponse
import com.devlab74.blogx.util.ApiSuccessResponse
import com.devlab74.blogx.util.ErrorHandling.Companion.ERROR_UNKNOWN
import javax.inject.Inject

class AuthRepository
@Inject
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val blogxAuthService: BlogxAuthService,
    val sessionManager: SessionManager
){
    fun attemptLogin(username: String, password: String): LiveData<DataState<AuthViewState>> {
        return blogxAuthService.login(username = username, password = password)
            .switchMap { response ->
                object : LiveData<DataState<AuthViewState>>() {
                    override fun onActive() {
                        super.onActive()
                        when(response) {
                            is ApiSuccessResponse -> {
                                value = DataState.data(
                                    data = AuthViewState(
                                        authToken = AuthToken(
                                            response.body.id!!,
                                            response.body.authToken
                                        )
                                    ),
                                    response = null
                                )
                            }
                            is ApiErrorResponse -> {
                                value = DataState.error(
                                    response = Response(
                                        message = response.errorMessage,
                                        responseType = ResponseType.Dialog()
                                    )
                                )
                            }
                            is ApiEmptyResponse -> {
                                value = DataState.error(
                                    response = Response(
                                        message = ERROR_UNKNOWN,
                                        responseType = ResponseType.Dialog()
                                    )
                                )
                            }
                        }
                    }
                }
            }
    }

    fun attemptRegistration(email: String, username: String, password: String, confirmPassword: String): LiveData<DataState<AuthViewState>> {
        return blogxAuthService.register(email = email, username = username, password = password, confirmPassword = confirmPassword)
            .switchMap { response ->
                object: LiveData<DataState<AuthViewState>>() {
                    override fun onActive() {
                        super.onActive()
                        when(response) {
                            is ApiSuccessResponse -> {
                                value = DataState.data(
                                    data = AuthViewState(
                                        authToken = AuthToken(
                                            response.body.id!!,
                                            response.body.authToken
                                        )
                                    ),
                                    response = null
                                )
                            }
                            is ApiErrorResponse -> {
                                value = DataState.error(
                                    response = Response(
                                        message = response.errorMessage,
                                        responseType = ResponseType.Dialog()
                                    )
                                )
                            }
                            is ApiEmptyResponse -> {
                                value = DataState.error(
                                    response = Response(
                                        message = ERROR_UNKNOWN,
                                        responseType = ResponseType.Dialog()
                                    )
                                )
                            }
                        }
                    }
                }
            }
    }
}