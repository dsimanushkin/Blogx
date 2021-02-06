package com.devlab74.blogx.repository.auth

import android.app.Application
import android.content.SharedPreferences
import com.devlab74.blogx.api.auth.BlogxAuthService
import com.devlab74.blogx.api.auth.network_responses.LoginResponse
import com.devlab74.blogx.api.auth.network_responses.RegistrationResponse
import com.devlab74.blogx.di.auth.AuthScope
import com.devlab74.blogx.models.AccountProperties
import com.devlab74.blogx.models.AuthToken
import com.devlab74.blogx.persistence.AccountPropertiesDao
import com.devlab74.blogx.persistence.AuthTokenDao
import com.devlab74.blogx.repository.buildError
import com.devlab74.blogx.repository.safeApiCall
import com.devlab74.blogx.repository.safeCacheCall
import com.devlab74.blogx.session.SessionManager
import com.devlab74.blogx.ui.auth.state.AuthViewState
import com.devlab74.blogx.ui.auth.state.LoginFields
import com.devlab74.blogx.ui.auth.state.RegistrationFields
import com.devlab74.blogx.util.*
import com.devlab74.blogx.util.ErrorHandling.Companion.handleErrors
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

@FlowPreview
@AuthScope
class AuthRepositoryImpl
@Inject
constructor(
    val application: Application,
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val blogxAuthService: BlogxAuthService,
    val sessionManager: SessionManager,
    val sharedPreferences: SharedPreferences,
    val sharedPrefsEditor: SharedPreferences.Editor
): AuthRepository {

    override fun attemptLogin(
        stateEvent: StateEvent,
        username: String,
        password: String
    ): Flow<DataState<AuthViewState>> = flow {

        val loginFieldErrors = LoginFields(username, password).isValidForLogin(application)
        if(loginFieldErrors == LoginFields.LoginError.none(application)) {
        val apiResult = safeApiCall(IO){
            blogxAuthService.login(
                username = username,
                password = password
            )
        }
        emit(
            object: ApiResponseHandler<AuthViewState, LoginResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ) {
                override suspend fun handleSuccess(resultObj: LoginResponse): DataState<AuthViewState> {

                    Timber.d("handleSuccess ")

                    // Login Errors counts as 200 response from server, need to handle that
                    if(resultObj.status == handleErrors(9005, application)){
                        return DataState.error(
                            response = Response(
                                handleErrors(resultObj.statusCode, application),
                                UIComponentType.Dialog(),
                                MessageType.Error()
                            ),
                            stateEvent = stateEvent
                        )
                    }
                    accountPropertiesDao.insertOrIgnore(
                        AccountProperties(
                            resultObj.id!!,
                            resultObj.email!!,
                            resultObj.username!!
                        )
                    )

                    // will return -1 if failure
                    val authToken = AuthToken(
                        resultObj.id!!,
                        resultObj.authToken!!
                    )

                    val result = authTokenDao.insert(authToken)

                    if(result < 0){
                        return DataState.error(
                            response = Response(
                                handleErrors(9006, application),
                                UIComponentType.Dialog(),
                                MessageType.Error()
                            ),
                            stateEvent = stateEvent
                        )
                    }

                    saveAuthenticatedUserToPrefs(username)

                    return DataState.data(
                        data = AuthViewState(
                            authToken = authToken
                        ),
                        stateEvent = stateEvent,
                        response = null
                    )
                }

            }.getResult()
        )
    }
        else{
            Timber.d("emitting error: $loginFieldErrors")
            emit(
                buildError(
                    loginFieldErrors,
                    UIComponentType.Dialog(),
                    stateEvent
                )
            )
        }
    }

    override fun attemptRegistration(
        stateEvent: StateEvent,
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): Flow<DataState<AuthViewState>> = flow {
        val registrationFieldErrors = RegistrationFields(email, username, password, confirmPassword).isValidForRegistration(application)
        if(registrationFieldErrors == RegistrationFields.RegistrationError.none(application)){
            val apiResult = safeApiCall(IO){
                blogxAuthService.register(
                    email = email,
                    username = username,
                    password = password,
                    confirmPassword = confirmPassword
                )
            }
            emit(
                object: ApiResponseHandler<AuthViewState, RegistrationResponse>(
                    response = apiResult,
                    stateEvent = stateEvent
                ){
                    override suspend fun handleSuccess(resultObj: RegistrationResponse): DataState<AuthViewState> {
                        if(resultObj.status == handleErrors(9005, application)){
                            return DataState.error(
                                response = Response(
                                    handleErrors(resultObj.statusCode, application),
                                    UIComponentType.Dialog(),
                                    MessageType.Error()
                                ),
                                stateEvent = stateEvent
                            )
                        }
                        val result1 = accountPropertiesDao.insertAndReplace(
                            AccountProperties(
                                resultObj.id!!,
                                resultObj.email!!,
                                resultObj.username!!
                            )
                        )
                        // will return -1 if failure
                        if(result1 < 0){
                            return DataState.error(
                                response = Response(
                                    handleErrors(9007, application),
                                    UIComponentType.Dialog(),
                                    MessageType.Error()
                                ),
                                stateEvent = stateEvent
                            )
                        }

                        // will return -1 if failure
                        val authToken = AuthToken(
                            resultObj.id!!,
                            resultObj.authToken
                        )
                        val result2 = authTokenDao.insert(authToken)
                        if(result2 < 0){
                            return DataState.error(
                                response = Response(
                                    handleErrors(9006, application),
                                    UIComponentType.Dialog(),
                                    MessageType.Error()
                                ),
                                stateEvent = stateEvent
                            )
                        }
                        saveAuthenticatedUserToPrefs(username)
                        return DataState.data(
                            data = AuthViewState(
                                authToken = authToken
                            ),
                            stateEvent = stateEvent,
                            response = null
                        )
                    }
                }.getResult()
            )
        }
        else{
            emit(
                buildError(
                    registrationFieldErrors,
                    UIComponentType.Dialog(),
                    stateEvent
                )
            )
        }
    }

    override fun checkPreviousAuthUser(
        stateEvent: StateEvent
    ): Flow<DataState<AuthViewState>> = flow {

        val previousAuthUserUsername: String? = sharedPreferences.getString(PreferenceKeys.PREVIOUS_AUTH_USER, null)
        Timber.d("checkPreviousAuthUser: $previousAuthUserUsername")
        if(previousAuthUserUsername.isNullOrBlank()){
            Timber.d("checkPreviousAuthUser: No previously authenticated user found.")
            emit(returnNoTokenFound(stateEvent))
        }
        else{
            Timber.d("checkPreviousAuthUser: Inside of Else statement")
            // TEST
            val cacheRes = accountPropertiesDao.searchByUsername(previousAuthUserUsername)
            Timber.d("checkPreviousAuthUser: CacheRes: $cacheRes")


            val apiResult = safeCacheCall(IO){
                accountPropertiesDao.searchByUsername(previousAuthUserUsername)
            }
            Timber.d("checkPreviousAuthUser: apiResult: $apiResult")
            emit(
                object: CacheResponseHandler<AuthViewState, AccountProperties>(
                    response = apiResult,
                    stateEvent = stateEvent
                ){
                    override suspend fun handleSuccess(resultObj: AccountProperties): DataState<AuthViewState> {
                        Timber.d("createCacheRequestAndReturn: ResultObject: $resultObj")
                        if(resultObj.id.isNotEmpty()){
                            authTokenDao.searchById(resultObj.id).let { authToken ->
                                if(authToken != null){
                                    if(authToken.authToken != null){
                                        return DataState.data(
                                            data = AuthViewState(
                                                authToken = authToken
                                            ),
                                            response = null,
                                            stateEvent = stateEvent
                                        )
                                    }
                                }
                            }
                        }
                        Timber.d("createCacheRequestAndReturn: AuthToken not found...")
                        return DataState.error(
                            response = Response(
                                handleErrors(9008, application),
                                UIComponentType.None(),
                                MessageType.Error()
                            ),
                            stateEvent = stateEvent
                        )
                    }
                }.getResult()
            )
        }
    }

    override fun saveAuthenticatedUserToPrefs(username: String) {
        sharedPrefsEditor.putString(PreferenceKeys.PREVIOUS_AUTH_USER, username)
        sharedPrefsEditor.apply()
    }

    override fun returnNoTokenFound(stateEvent: StateEvent): DataState<AuthViewState> {
        return DataState.error(
            response = Response(
                handleErrors(9008, application),
                UIComponentType.None(),
                MessageType.Error()
            ),
            stateEvent = stateEvent
        )
    }
}