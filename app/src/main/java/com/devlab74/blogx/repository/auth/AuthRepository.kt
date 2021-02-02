package com.devlab74.blogx.repository.auth

import android.app.Application
import androidx.lifecycle.LiveData
import com.devlab74.blogx.api.auth.BlogxAuthService
import com.devlab74.blogx.api.auth.network_responses.LoginResponse
import com.devlab74.blogx.api.auth.network_responses.RegistrationResponse
import com.devlab74.blogx.models.AuthToken
import com.devlab74.blogx.persistence.AccountPropertiesDao
import com.devlab74.blogx.persistence.AuthTokenDao
import com.devlab74.blogx.repository.NetworkBoundResource
import com.devlab74.blogx.session.SessionManager
import com.devlab74.blogx.ui.DataState
import com.devlab74.blogx.ui.Response
import com.devlab74.blogx.ui.ResponseType
import com.devlab74.blogx.ui.auth.state.AuthViewState
import com.devlab74.blogx.ui.auth.state.LoginFields
import com.devlab74.blogx.ui.auth.state.RegistrationFields
import com.devlab74.blogx.util.ApiSuccessResponse
import com.devlab74.blogx.util.ErrorHandling.Companion.GENERIC_ERROR
import com.devlab74.blogx.util.GenericApiResponse
import kotlinx.coroutines.Job
import timber.log.Timber
import javax.inject.Inject

class AuthRepository
@Inject
constructor(
    val application: Application,
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val blogxAuthService: BlogxAuthService,
    val sessionManager: SessionManager
){

    private var repositoryJob: Job? = null

    fun attemptLogin(username: String, password: String): LiveData<DataState<AuthViewState>> {
        val loginFieldErrors = LoginFields(username, password).isValidForLogin(application)
        if (loginFieldErrors != LoginFields.LoginError.none(application)) {
            return returnErrorResponse(loginFieldErrors, ResponseType.Dialog())
        }

        return object : NetworkBoundResource<LoginResponse, AuthViewState>(
            application,
            sessionManager.isConnectedToTheInternet()
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<LoginResponse>) {
                Timber.d("handleApiSuccessResponse: $response")

                // Login Errors counts as 200 response from server, need to handle that
                if (response.body.status == GENERIC_ERROR) {
                    return onErrorReturn(errorMessage = null, statusCode = response.body.statusCode, shouldUseDialog = true, shouldUseToast = false, application = application)
                }

                onCompleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(response.body.id!!, response.body.authToken)
                        )
                    )
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<LoginResponse>> {
                return blogxAuthService.login(username = username, password = password)
            }

            override fun setJob(job: Job) {
                repositoryJob?.cancel()
                repositoryJob = job
            }
        }.asLiveData()
    }

    fun attemptRegistration(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): LiveData<DataState<AuthViewState>> {
        val registrationFieldErrors = RegistrationFields(email, username, password, confirmPassword).isValidForRegistration(application)
        if (registrationFieldErrors != RegistrationFields.RegistrationError.none(application)) {
            return returnErrorResponse(registrationFieldErrors, ResponseType.Dialog())
        }

        return object: NetworkBoundResource<RegistrationResponse, AuthViewState>(
            application,
            sessionManager.isConnectedToTheInternet()
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<RegistrationResponse>) {
                Timber.d("handleApiSuccessResponse: $response")

                if (response.body.status == GENERIC_ERROR) {
                    return onErrorReturn(errorMessage = null, statusCode = response.body.statusCode, shouldUseDialog = true, shouldUseToast = false, application = application)
                }

                onCompleteJob(
                    DataState.data(
                        data = AuthViewState(
                            authToken = AuthToken(response.body.id!!, response.body.authToken)
                        )
                    )
                )
            }

            override fun createCall(): LiveData<GenericApiResponse<RegistrationResponse>> {
                return blogxAuthService.register(email = email, username = username, password = password, confirmPassword = confirmPassword)
            }

            override fun setJob(job: Job) {
                repositoryJob?.cancel()
                repositoryJob = job
            }

        }.asLiveData()
    }

    fun returnErrorResponse(errorMessage: String, responseType: ResponseType): LiveData<DataState<AuthViewState>> {
        Timber.d("returnErrorResponse: $errorMessage")

        return object : LiveData<DataState<AuthViewState>>() {
            override fun onActive() {
                super.onActive()
                value = DataState.error(
                    Response(
                        message = errorMessage,
                        responseType = responseType
                    )
                )
            }
        }
    }

    fun cancelActiveJobs() {
        Timber.d("AuthRepository: Cancelling on-going jobs...")
        repositoryJob?.cancel()
    }
}