package com.devlab74.blogx.repository.main

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.switchMap
import com.devlab74.blogx.api.main.BlogxMainService
import com.devlab74.blogx.api.main.response.BlogListSearchResponse
import com.devlab74.blogx.models.AuthToken
import com.devlab74.blogx.models.BlogPost
import com.devlab74.blogx.persistence.BlogPostDao
import com.devlab74.blogx.repository.JobManager
import com.devlab74.blogx.repository.NetworkBoundResource
import com.devlab74.blogx.session.SessionManager
import com.devlab74.blogx.ui.DataState
import com.devlab74.blogx.ui.main.blog.state.BlogViewState
import com.devlab74.blogx.util.ApiSuccessResponse
import com.devlab74.blogx.util.DateUtils
import com.devlab74.blogx.util.GenericApiResponse
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.Dispatchers.Main
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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
        query: String
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
                return blogPostDao.getAllBlogPosts()
                    .switchMap {
                        object : LiveData<BlogViewState>() {
                            override fun onActive() {
                                super.onActive()
                                value = BlogViewState(
                                    BlogViewState.BlogFields(
                                        blogList = it
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
                    query = query
                )
            }

            override fun setJob(job: Job) {
                addJob("searchBlogPost", job)
            }

        }.asLiveData()
    }

}