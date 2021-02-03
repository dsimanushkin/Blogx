package com.devlab74.blogx.repository.main

import com.devlab74.blogx.api.main.BlogxMainService
import com.devlab74.blogx.persistence.AccountPropertiesDao
import com.devlab74.blogx.session.SessionManager
import kotlinx.coroutines.Job
import timber.log.Timber
import javax.inject.Inject

class AccountRepository
@Inject
constructor(
    val blogxMainService: BlogxMainService,
    val accountPropertiesDao: AccountPropertiesDao,
    val sessionManager: SessionManager
) {
    private var repositoryJob: Job? = null

    fun cancelActiveJobs() {
        Timber.d("AccountRepository: cancelling on-going jobs...")
        repositoryJob?.cancel()
    }
}