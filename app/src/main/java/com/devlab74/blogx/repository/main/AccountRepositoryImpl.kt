package com.devlab74.blogx.repository.main

import android.app.Application
import android.content.SharedPreferences
import com.devlab74.blogx.api.GenericResponse
import com.devlab74.blogx.api.main.BlogxMainService
import com.devlab74.blogx.di.main.MainScope
import com.devlab74.blogx.models.AccountProperties
import com.devlab74.blogx.models.AuthToken
import com.devlab74.blogx.persistence.AccountPropertiesDao
import com.devlab74.blogx.repository.NetworkBoundResource
import com.devlab74.blogx.repository.safeApiCall
import com.devlab74.blogx.session.SessionManager
import com.devlab74.blogx.ui.main.account.state.AccountViewState
import com.devlab74.blogx.util.*
import com.devlab74.blogx.util.ErrorHandling.Companion.handleErrors
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import timber.log.Timber
import javax.inject.Inject

@FlowPreview
@MainScope
class AccountRepositoryImpl
@Inject
constructor(
    val application: Application,
    val blogxMainService: BlogxMainService,
    val accountPropertiesDao: AccountPropertiesDao,
    val sessionManager: SessionManager,
    private val sharedPrefsEditor: SharedPreferences.Editor
): AccountRepository {

    override fun getAccountProperties(
        authToken: AuthToken,
        stateEvent: StateEvent
    ): Flow<DataState<AccountViewState>> {
        return object: NetworkBoundResource<AccountProperties, AccountProperties, AccountViewState>(
            dispatcher = IO,
            stateEvent = stateEvent,
            apiCall = {
                blogxMainService.getAccountProperties(authorization = authToken.authToken!!)
            },
            cacheCall = {
                accountPropertiesDao.searchById(authToken.accountId)
            },
            application = application
        ){
            override suspend fun updateCache(networkObject: AccountProperties) {
                Timber.d("updateCache: $networkObject")
                accountPropertiesDao.updateAccountProperties(
                    networkObject.id,
                    networkObject.email,
                    networkObject.username
                )
            }

            override fun handleCacheSuccess(
                resultObj: AccountProperties
            ): DataState<AccountViewState> {
                return DataState.data(
                    response = null,
                    data = AccountViewState(
                        accountProperties = resultObj
                    ),
                    stateEvent = stateEvent
                )
            }
        }.result
    }

    override fun saveAccountProperties(
        authToken: AuthToken,
        email: String,
        username: String,
        stateEvent: StateEvent
    ) = flow{
        val apiResult = safeApiCall(IO){
            blogxMainService.saveAccountProperties(
                authorization = authToken.authToken!!,
                email = email,
                username = username
            )
        }
        emit(
            object: ApiResponseHandler<AccountViewState, GenericResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ){
                override suspend fun handleSuccess(
                    resultObj: GenericResponse
                ): DataState<AccountViewState> {

                    if(resultObj.status == handleErrors(9005, application)){
                        return DataState.error(
                            response = Response(
                                handleErrors(resultObj.statusCode, application),
                                UIComponentType.Dialog,
                                MessageType.Error
                            ),
                            stateEvent = stateEvent
                        )
                    }

                    val updatedAccountProperties = blogxMainService.getAccountProperties(authorization = authToken.authToken!!)

                    accountPropertiesDao.updateAccountProperties(
                        id = updatedAccountProperties.id,
                        email = updatedAccountProperties.email,
                        username = updatedAccountProperties.username
                    )

                    saveUpdatedUserToPrefs(updatedAccountProperties.username)

                    return DataState.data(
                        data = null,
                        response = Response(
                            message = handleErrors(resultObj.statusCode, application),
                            uiComponentType = UIComponentType.Toast,
                            messageType = MessageType.Success
                        ),
                        stateEvent = stateEvent
                    )
                }
            }.getResult()
        )
    }

    override fun updatePassword(
        authToken: AuthToken,
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String,
        stateEvent: StateEvent
    ) = flow {
        val apiResult = safeApiCall(IO){
            blogxMainService.updatePassword(
                authorization = authToken.authToken!!,
                currentPassword = currentPassword,
                newPassword = newPassword,
                confirmNewPassword = confirmNewPassword
            )
        }
        emit(
            object: ApiResponseHandler<AccountViewState, GenericResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ){
                override suspend fun handleSuccess(
                    resultObj: GenericResponse
                ): DataState<AccountViewState> {

                    if(resultObj.status == handleErrors(9005, application)){
                        return DataState.error(
                            response = Response(
                                handleErrors(resultObj.statusCode, application),
                                UIComponentType.Dialog,
                                MessageType.Error
                            ),
                            stateEvent = stateEvent
                        )
                    }

                    return DataState.data(
                        data = null,
                        response = Response(
                            message = handleErrors(resultObj.statusCode, application),
                            uiComponentType = UIComponentType.Toast,
                            messageType = MessageType.Success
                        ),
                        stateEvent = stateEvent
                    )
                }
            }.getResult()
        )
    }

    override fun saveUpdatedUserToPrefs(username: String) {
        sharedPrefsEditor.putString(PreferenceKeys.PREVIOUS_AUTH_USER, username)
        sharedPrefsEditor.apply()
    }
}