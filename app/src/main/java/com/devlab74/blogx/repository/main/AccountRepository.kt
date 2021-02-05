package com.devlab74.blogx.repository.main

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.devlab74.blogx.api.GenericResponse
import com.devlab74.blogx.api.main.BlogxMainService
import com.devlab74.blogx.di.main.MainScope
import com.devlab74.blogx.models.AccountProperties
import com.devlab74.blogx.models.AuthToken
import com.devlab74.blogx.persistence.AccountPropertiesDao
import com.devlab74.blogx.repository.JobManager
import com.devlab74.blogx.repository.NetworkBoundResource
import com.devlab74.blogx.session.SessionManager
import com.devlab74.blogx.ui.DataState
import com.devlab74.blogx.ui.Response
import com.devlab74.blogx.ui.ResponseType
import com.devlab74.blogx.ui.main.account.state.AccountViewState
import com.devlab74.blogx.util.AbsentLiveData
import com.devlab74.blogx.util.ApiSuccessResponse
import com.devlab74.blogx.util.ErrorHandling.Companion.handleErrors
import com.devlab74.blogx.util.GenericApiResponse
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

@MainScope
class AccountRepository
@Inject
constructor(
    val application: Application,
    val blogxMainService: BlogxMainService,
    val accountPropertiesDao: AccountPropertiesDao,
    val sessionManager: SessionManager
): JobManager("AccountRepository") {

    fun getAccountProperties(authToken: AuthToken): LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<AccountProperties, AccountProperties, AccountViewState>(
            application,
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ) {
            override suspend fun createCacheRequestAndReturn() {
                withContext(Main) {
                    // Finish by viewing the db cache
                    result.addSource(loadFromCache()) {viewState ->
                        onCompleteJob(DataState.data(
                            data = viewState,
                            response = null
                        ))
                    }
                }
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<AccountProperties>) {
                updateLocalDb(response.body)

                createCacheRequestAndReturn()
            }

            override fun createCall(): LiveData<GenericApiResponse<AccountProperties>> {
                return blogxMainService.getAccountProperties(authorization = authToken.authToken!!)
            }

            override fun setJob(job: Job) {
                addJob("getAccountProperties", job)
            }

            override fun loadFromCache(): LiveData<AccountViewState> {
                return accountPropertiesDao.searchById(authToken.accountId)
                    .switchMap {
                        object : LiveData<AccountViewState>() {
                            override fun onActive() {
                                super.onActive()
                                value = AccountViewState(it)
                            }
                        }
                    }
            }

            override suspend fun updateLocalDb(cachedObject: AccountProperties?) {
                cachedObject?.let {
                    accountPropertiesDao.updateAccountProperties(
                        cachedObject.id,
                        cachedObject.email,
                        cachedObject.username
                    )
                }
            }

        }.asLiveData()
    }

    fun saveAccountProperties(
        authToken: AuthToken,
        accountProperties: AccountProperties
    ): LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<GenericResponse, Any, AccountViewState>(
            application,
            sessionManager.isConnectedToTheInternet(),
            true,
            false,
            true
        ) {
            // Not in use in this case
            override suspend fun createCacheRequestAndReturn() {

            }

            // Not in use in this case
            override fun loadFromCache(): LiveData<AccountViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cachedObject: Any?) {
                return accountPropertiesDao.updateAccountProperties(
                    accountProperties.id,
                    accountProperties.email,
                    accountProperties.username
                )
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {

                if (response.body.status == handleErrors(9005, application)) {
                    return onErrorReturn(errorMessage = null, statusCode = response.body.statusCode, shouldUseDialog = true, shouldUseToast = false, application = application)
                }

                updateLocalDb(null)

                withContext(Main) {
                    // Finish with success response
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(
                                handleErrors(response.body.statusCode, application),
                                ResponseType.Toast()
                            )
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return blogxMainService.saveAccountProperties(
                    authorization = authToken.authToken!!,
                    email = accountProperties.email,
                    username = accountProperties.username
                )
            }

            override fun setJob(job: Job) {
                addJob("saveAccountProperties", job)
            }

        }.asLiveData()
    }

    fun updatePassword(
        authToken: AuthToken,
        currentPassword: String,
        newPassword: String,
        confirmNewPassword: String
    ): LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<GenericResponse, Any, AccountViewState>(
            application,
            sessionManager.isConnectedToTheInternet(),
            true,
            true,
            false
        ) {
            // Not in use in this case
            override suspend fun createCacheRequestAndReturn() {

            }

            // Not in use in this case
            override fun loadFromCache(): LiveData<AccountViewState> {
                return AbsentLiveData.create()
            }

            // Not in use in this case
            override suspend fun updateLocalDb(cachedObject: Any?) {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {

                if (response.body.status == handleErrors(9005, application)) {
                    return onErrorReturn(errorMessage = null, statusCode = response.body.statusCode, shouldUseDialog = true, shouldUseToast = false, application = application)
                }

                withContext(Main) {
                    // Finish with success response
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(
                                handleErrors(response.body.statusCode, application),
                                ResponseType.Toast()
                            )
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return blogxMainService.updatePassword(
                    authorization = authToken.authToken!!,
                    currentPassword = currentPassword,
                    newPassword = newPassword,
                    confirmNewPassword = confirmNewPassword
                )
            }

            override fun setJob(job: Job) {
                addJob("updatePassword", job)
            }

        }.asLiveData()
    }
}