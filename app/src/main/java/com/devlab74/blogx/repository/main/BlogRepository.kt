package com.devlab74.blogx.repository.main

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.devlab74.blogx.api.GenericResponse
import com.devlab74.blogx.api.main.BlogxMainService
import com.devlab74.blogx.api.main.response.BlogCreateUpdateResponse
import com.devlab74.blogx.api.main.response.BlogIsAuthorResponse
import com.devlab74.blogx.api.main.response.BlogListSearchResponse
import com.devlab74.blogx.models.AuthToken
import com.devlab74.blogx.models.BlogPost
import com.devlab74.blogx.persistence.BlogPostDao
import com.devlab74.blogx.persistence.returnOrderedBlogQuery
import com.devlab74.blogx.repository.JobManager
import com.devlab74.blogx.repository.NetworkBoundResource
import com.devlab74.blogx.session.SessionManager
import com.devlab74.blogx.ui.DataState
import com.devlab74.blogx.ui.Response
import com.devlab74.blogx.ui.ResponseType
import com.devlab74.blogx.ui.main.blog.state.BlogViewState
import com.devlab74.blogx.util.AbsentLiveData
import com.devlab74.blogx.util.ApiSuccessResponse
import com.devlab74.blogx.util.Constants.Companion.PAGINATION_PAGE_SIZE
import com.devlab74.blogx.util.DateUtils
import com.devlab74.blogx.util.ErrorHandling.Companion.handleErrors
import com.devlab74.blogx.util.GenericApiResponse
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

class BlogRepository
@Inject
constructor(
    val application: Application,
    val blogxMainService: BlogxMainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager
): JobManager("BlogRepository") {

    fun searchBlogPost(
        authToken: AuthToken,
        query: String,
        filterAndOrder: String,
        page: Int
    ): LiveData<DataState<BlogViewState>> {
        return object : NetworkBoundResource<BlogListSearchResponse, List<BlogPost>, BlogViewState>(
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
                        viewState.blogFields.isQueryInProgress = false
                        if (page * PAGINATION_PAGE_SIZE > viewState.blogFields.blogList.size) {
                            viewState.blogFields.isQueryExhausted = true
                        }
                        onCompleteJob(
                            DataState.data(
                                viewState,
                                null
                            )
                        )
                    }
                }
            }

            override fun loadFromCache(): LiveData<BlogViewState> {
                return blogPostDao.returnOrderedBlogQuery(
                    query,
                    filterAndOrder,
                    page
                )
                    .switchMap {
                        object : LiveData<BlogViewState>() {
                            override fun onActive() {
                                super.onActive()
                                value = BlogViewState(
                                    BlogViewState.BlogFields(
                                        blogList = it,
                                        isQueryInProgress = true
                                    )
                                )
                            }
                        }
                    }
            }

            override suspend fun updateLocalDb(cachedObject: List<BlogPost>?) {
                if (cachedObject != null) {
                    withContext(IO) {
                        for (blogPost in cachedObject) {
                            try {
                                // Launch each insert as a separate job to execute in parallel
                                val j = launch {
                                    Timber.d("updateLocalDb: inserting blog: $blogPost")
                                    blogPostDao.insert(blogPost)
                                }
                                j.join()
                            } catch (e: Exception) {
                                Timber.e("updateLocalDb: error updating cache on blog post with slug: ${blogPost.id}")
                            }
                        }
                    }
                }
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<BlogListSearchResponse>) {
                val blogPostList: ArrayList<BlogPost> = ArrayList()

                for (blogPostResponse in response.body.results) {
                    blogPostList.add(
                        BlogPost(
                            id = blogPostResponse.id,
                            title = blogPostResponse.title,
                            body = blogPostResponse.body,
                            image = blogPostResponse.image,
                            dateUpdated = DateUtils.convertServerStringDateToLong(blogPostResponse.dateUpdated),
                            username = blogPostResponse.username
                        )
                    )
                }
                updateLocalDb(blogPostList)
                createCacheRequestAndReturn()
            }

            override fun createCall(): LiveData<GenericApiResponse<BlogListSearchResponse>> {
                return blogxMainService.searchListBlogPosts(
                    authorization = authToken.authToken!!,
                    query = query,
                    ordering = filterAndOrder,
                    page = page
                )
            }

            override fun setJob(job: Job) {
                addJob("searchBlogPost", job)
            }

        }.asLiveData()
    }

    fun isAuthorOfBlogPost(
        authToken: AuthToken,
        blogId: String
    ): LiveData<DataState<BlogViewState>> {
        return object : NetworkBoundResource<BlogIsAuthorResponse, Any, BlogViewState>(
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
            override fun loadFromCache(): LiveData<BlogViewState> {
                return AbsentLiveData.create()
            }

            // Not in use in this case
            override suspend fun updateLocalDb(cachedObject: Any?) {

            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<BlogIsAuthorResponse>) {
                withContext(Main) {
                    Timber.d("handleApiSuccessResponse: ${response.body.isAuthorOfBlogPost}")
                    onCompleteJob(
                        DataState.data(
                            data = BlogViewState(
                                viewBlogFields = BlogViewState.ViewBlogFields(
                                    isAuthorOfBlogPost = response.body.isAuthorOfBlogPost
                                )
                            ),
                            response = null
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<BlogIsAuthorResponse>> {
                return blogxMainService.isAuthorOfBlogPost(
                    authorization = authToken.authToken!!,
                    blogId = blogId
                )
            }

            override fun setJob(job: Job) {
                addJob("isAuthorOfBlogPost", job)
            }

        }.asLiveData()
    }

    fun deleteBlogPost(
        authToken: AuthToken,
        blogPost: BlogPost
    ): LiveData<DataState<BlogViewState>> {
        return object : NetworkBoundResource<GenericResponse, BlogPost, BlogViewState>(
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
            override fun loadFromCache(): LiveData<BlogViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cachedObject: BlogPost?) {
                cachedObject?.let { blogPost ->
                    blogPostDao.deleteBlogPost(blogPost)
                    onCompleteJob(
                        DataState.data(
                            data = null,
                            response = Response(
                                message = handleErrors(4003, application),
                                responseType = ResponseType.Toast()
                            )
                        )
                    )
                }
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<GenericResponse>) {
                if (response.body.statusCode == 4003) {
                    updateLocalDb(blogPost)
                } else {
                    onCompleteJob(
                        DataState.error(
                            Response(
                                message = handleErrors(response.body.statusCode, application),
                                responseType = ResponseType.Dialog()
                            )
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<GenericResponse>> {
                return blogxMainService.deleteBlogPost(
                    authorization = authToken.authToken!!,
                    blogId = blogPost.id
                )
            }

            override fun setJob(job: Job) {
                addJob("deleteBlogPost", job)
            }

        }.asLiveData()
    }

    fun updateBlogPost(
        authToken: AuthToken,
        blogId: String,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?
    ): LiveData<DataState<BlogViewState>> {
        return object : NetworkBoundResource<BlogCreateUpdateResponse, BlogPost, BlogViewState>(
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
            override fun loadFromCache(): LiveData<BlogViewState> {
                return AbsentLiveData.create()
            }

            override suspend fun updateLocalDb(cachedObject: BlogPost?) {
                cachedObject?.let { blogPost ->
                    blogPostDao.updateBlogPost(
                        blogPost.id,
                        blogPost.title,
                        blogPost.body,
                        blogPost.image
                    )
                }
            }

            override suspend fun handleApiSuccessResponse(response: ApiSuccessResponse<BlogCreateUpdateResponse>) {
                val updatedBlogPost = BlogPost(
                    response.body.id,
                    response.body.title,
                    response.body.body,
                    response.body.image,
                    DateUtils.convertServerStringDateToLong(
                        response.body.dateUpdated
                    ),
                    response.body.username
                )

                updateLocalDb(updatedBlogPost)

                withContext(Main) {
                    onCompleteJob(
                        DataState.data(
                            data = BlogViewState(
                                viewBlogFields = BlogViewState.ViewBlogFields(
                                    blogPost = updatedBlogPost
                                )
                            ),
                            response = Response(
                                handleErrors(response.body.statusCode, application),
                                ResponseType.Toast()
                            )
                        )
                    )
                }
            }

            override fun createCall(): LiveData<GenericApiResponse<BlogCreateUpdateResponse>> {
                return blogxMainService.updateBlog(
                    authorization = authToken.authToken!!,
                    blogId = blogId,
                    title = title,
                    body = body,
                    image = image
                )
            }

            override fun setJob(job: Job) {
                addJob("updateBlogPost", job)
            }

        }.asLiveData()
    }

}