package com.devlab74.blogx.repository.main

import android.app.Application
import androidx.lifecycle.LiveData
import com.devlab74.blogx.api.main.BlogxMainService
import com.devlab74.blogx.api.main.response.BlogCreateUpdateResponse
import com.devlab74.blogx.models.AuthToken
import com.devlab74.blogx.models.BlogPost
import com.devlab74.blogx.persistence.BlogPostDao
import com.devlab74.blogx.repository.JobManager
import com.devlab74.blogx.repository.NetworkBoundResource
import com.devlab74.blogx.session.SessionManager
import com.devlab74.blogx.ui.DataState
import com.devlab74.blogx.ui.Response
import com.devlab74.blogx.ui.ResponseType
import com.devlab74.blogx.ui.main.create_blog.state.CreateBlogViewState
import com.devlab74.blogx.util.AbsentLiveData
import com.devlab74.blogx.util.ApiSuccessResponse
import com.devlab74.blogx.util.DateUtils
import com.devlab74.blogx.util.ErrorHandling.Companion.handleErrors
import com.devlab74.blogx.util.GenericApiResponse
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber
import javax.inject.Inject

class CreateBlogRepository
@Inject
constructor(
    val application: Application,
    val blogxMainService: BlogxMainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager
): JobManager("CreateBlogRepository") {

    fun createNewBlogPost(
        authToken: AuthToken,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?
    ): LiveData<DataState<CreateBlogViewState>> {
        return object : NetworkBoundResource<BlogCreateUpdateResponse, BlogPost, CreateBlogViewState>(
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
            override fun loadFromCache(): LiveData<CreateBlogViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cachedObject: BlogPost?) {
                cachedObject?.let {
                    blogPostDao.insert(it)
                }
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<BlogCreateUpdateResponse>) {
                Timber.d("RESPONSE CREATE BLOG: $response")

                val updateBlogPost = BlogPost(
                    response.body.id,
                    response.body.title,
                    response.body.body,
                    response.body.image,
                    DateUtils.convertServerStringDateToLong(response.body.dateUpdated),
                    response.body.username
                )
                updateLocalDb(updateBlogPost)
                withContext(Main) {
                    // Finish with success response
                    onCompleteJob(
                        DataState.data(
                            null,
                            Response(
                                handleErrors(response.body.statusCode, application),
                                ResponseType.Dialog()
                            )
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<BlogCreateUpdateResponse>> {
                return blogxMainService.createBlog(
                    authorization = authToken.authToken!!,
                    title = title,
                    body = body,
                    image = image
                )
            }

            override fun setJob(job: Job) {
                addJob("createNewBlogPost", job)
            }

        }.asLiveData()
    }

}