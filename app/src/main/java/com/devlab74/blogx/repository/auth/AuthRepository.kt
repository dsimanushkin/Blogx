package com.devlab74.blogx.repository.auth

import android.app.Application
import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.devlab74.blogx.api.auth.BlogxAuthService
import com.devlab74.blogx.api.auth.network_responses.LoginResponse
import com.devlab74.blogx.api.auth.network_responses.RegistrationResponse
import com.devlab74.blogx.models.AccountProperties
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
import com.devlab74.blogx.util.AbsentLiveData
import com.devlab74.blogx.util.ApiSuccessResponse
import com.devlab74.blogx.util.ErrorHandling.Companion.handleErrors
import com.devlab74.blogx.util.GenericApiResponse
import com.devlab74.blogx.util.PreferenceKeys
import com.devlab74.blogx.util.SuccessHandling.Companion.RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE
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
    val sessionManager: SessionManager,
    val sharedPreferences: SharedPreferences,
    val sharedPrefsEditor: SharedPreferences.Editor
){

    private var repositoryJob: Job? = null

    fun attemptLogin(username: String, password: String): LiveData<DataState<AuthViewState>> {
        val loginFieldErrors = LoginFields(username, password).isValidForLogin(application)
        if (loginFieldErrors != LoginFields.LoginError.none(application)) {
            return returnErrorResponse(loginFieldErrors, ResponseType.Dialog())
        }

        return object : NetworkBoundResource<LoginResponse, Any, AuthViewState>(
            application,
            sessionManager.isConnectedToTheInternet(),
            true,
            false,
            true
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<LoginResponse>) {
                Timber.d("handleApiSuccessResponse: $response")

                // Login Errors counts as 200 response from server, need to handle that
                if (response.body.status == handleErrors(9005, application)) {
                    return onErrorReturn(errorMessage = null, statusCode = response.body.statusCode, shouldUseDialog = true, shouldUseToast = false, application = application)
                }

                accountPropertiesDao.insertOrIgnore(
                    AccountProperties(
                        id = response.body.id!!,
                        email = response.body.email!!,
                        username = ""
                    )
                )

                val result = authTokenDao.insert(
                    AuthToken(
                        accountId = response.body.id!!,
                        authToken = response.body.authToken
                    )
                )

                if (result < 0) {
                    return onCompleteJob(
                        DataState.error(
                            Response(handleErrors(9006, application), ResponseType.Dialog())
                        )
                    )
                }

                saveAuthenticatedUserToPrefs(response.body.email!!)

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

            // Not in use in this case
            override suspend fun createCacheRequestAndReturn() {

            }

            // Not in use in this case
            override fun loadFromCache(): LiveData<AuthViewState> {
                return AbsentLiveData.create()
            }

            // Not in use in this case
            override suspend fun updateLocalDb(cachedObject: Any?) {

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

        return object: NetworkBoundResource<RegistrationResponse, Any, AuthViewState>(
            application,
            sessionManager.isConnectedToTheInternet(),
            true,
            false,
            true
        ) {
            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<RegistrationResponse>) {
                Timber.d("handleApiSuccessResponse: $response")

                if (response.body.status == handleErrors(9005, application)) {
                    return onErrorReturn(errorMessage = null, statusCode = response.body.statusCode, shouldUseDialog = true, shouldUseToast = false, application = application)
                }

                val result1 = accountPropertiesDao.insertAndReplace(
                    AccountProperties(
                        id = response.body.id!!,
                        email = response.body.email!!,
                        username = response.body.username!!
                    )
                )

                // Will return -1 if failure
                if (result1 < 0) {
                    return onCompleteJob(
                        DataState.error(
                            Response(handleErrors(9007, application), ResponseType.Dialog())
                        )
                    )
                }

                val result2 = authTokenDao.insert(
                    AuthToken(
                        accountId = response.body.id!!,
                        authToken = response.body.authToken
                    )
                )

                // Will return -1 if failure
                if (result2 < 0) {
                    return onCompleteJob(
                        DataState.error(
                            Response(handleErrors(9006, application), ResponseType.Dialog())
                        )
                    )
                }

                saveAuthenticatedUserToPrefs(response.body.email!!)

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

            // Not in use in this case
            override suspend fun createCacheRequestAndReturn() {

            }

            // Not in use in this case
            override fun loadFromCache(): LiveData<AuthViewState> {
                return AbsentLiveData.create()
            }

            // Not in use in this case
            override suspend fun updateLocalDb(cachedObject: Any?) {

            }

        }.asLiveData()
    }

    fun checkPreviousAuthUser(): LiveData<DataState<AuthViewState>> {
        val previousAuthUserEmail: String? = sharedPreferences.getString(PreferenceKeys.PREVIOUS_AUTH_USER, null)

        if (previousAuthUserEmail.isNullOrBlank()) {
            Timber.d("checkPreviousAuthUser: No previously authenticated user found...")
            return returnNoTokenFound()
        } else {
            return object : NetworkBoundResource<Void, Any, AuthViewState>(
                application,
                sessionManager.isConnectedToTheInternet(),
                false,
                false,
                false
            ) {
                override suspend fun createCacheRequestAndReturn() {
                    accountPropertiesDao.searchByEmail(previousAuthUserEmail).let { accountProperties ->
                        Timber.d("checkPreviousAuthUser: searching for token: $accountProperties")

                        accountProperties?.let {
                            if (accountProperties.id.isNotEmpty()) {
                                authTokenDao.searchById(accountProperties.id).let { authToken ->
                                    if (authToken != null) {
                                        if (authToken.authToken != null) {
                                            return onCompleteJob(
                                                DataState.data(
                                                    data = AuthViewState(
                                                        authToken = authToken
                                                    )
                                                )
                                            )
                                        }
                                    }
                                }
                            }
                        }
                        Timber.d("checkPreviousAuthUser: Auth token not found...")
                        onCompleteJob(
                            DataState.data(
                                data = null,
                                response = Response(
                                    RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE,
                                    ResponseType.None()
                                )
                            )
                        )
                    }
                }

                // Not in use in this case
                override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<Void>) {

                }

                // Not in use in this case
                override fun createCall(): LiveData<GenericApiResponse<Void>> {
                    return AbsentLiveData.create()
                }

                override fun setJob(job: Job) {
                    repositoryJob?.cancel()
                    repositoryJob = job
                }

                // Not in use in this case
                override fun loadFromCache(): LiveData<AuthViewState> {
                    return AbsentLiveData.create()
                }

                // Not in use in this case
                override suspend fun updateLocalDb(cachedObject: Any?) {

                }

            }.asLiveData()
        }
    }

    private fun returnNoTokenFound(): LiveData<DataState<AuthViewState>> {
        return object : LiveData<DataState<AuthViewState>>() {
            override fun onActive() {
                super.onActive()
                value = DataState.data(
                    data = null,
                    response = Response(RESPONSE_CHECK_PREVIOUS_AUTH_USER_DONE, ResponseType.None())
                )
            }
        }
    }

    private fun saveAuthenticatedUserToPrefs(email: String) {
        sharedPrefsEditor.putString(PreferenceKeys.PREVIOUS_AUTH_USER, email)
        sharedPrefsEditor.apply()
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