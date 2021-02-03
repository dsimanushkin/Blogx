package com.devlab74.blogx.repository.main

import android.app.Application
import com.devlab74.blogx.api.main.BlogxMainService
import com.devlab74.blogx.persistence.BlogPostDao
import com.devlab74.blogx.repository.JobManager
import com.devlab74.blogx.session.SessionManager
import javax.inject.Inject

class BlogRepository
@Inject
constructor(
    val application: Application,
    val blogxMainService: BlogxMainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager
): JobManager("BlogRepository") {
}