package com.devlab74.blogx.repository.main

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.devlab74.blogx.api.main.BlogxMainService
import com.devlab74.blogx.models.AccountProperties
import com.devlab74.blogx.models.AuthToken
import com.devlab74.blogx.persistence.AccountPropertiesDao
import com.devlab74.blogx.repository.NetworkBoundResource
import com.devlab74.blogx.session.SessionManager
import com.devlab74.blogx.ui.DataState
import com.devlab74.blogx.ui.main.account.state.AccountViewState
import com.devlab74.blogx.util.ApiSuccessResponse
import com.devlab74.blogx.util.GenericApiResponse
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import timber.log.Timber
import javax.inject.Inject

class AccountRepository
@Inject
constructor(
    val application: Application,
    val blogxMainService: BlogxMainService,
    val accountPropertiesDao: AccountPropertiesDao,
    val sessionManager: SessionManager
) {
    private var repositoryJob: Job? = null

    fun getAccountProperties(authToken: AuthToken): LiveData<DataState<AccountViewState>> {
        return object : NetworkBoundResource<AccountProperties, AccountProperties, AccountViewState>(
            application,
            sessionManager.isConnectedToTheInternet(),
            true,
            true
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
                repositoryJob?.cancel()
                repositoryJob = job
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

    fun cancelActiveJobs() {
        Timber.d("AccountRepository: cancelling on-going jobs...")
        repositoryJob?.cancel()
    }
}