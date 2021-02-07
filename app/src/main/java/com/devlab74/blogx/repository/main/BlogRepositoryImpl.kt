package com.devlab74.blogx.repository.main

import android.app.Application
import com.devlab74.blogx.api.GenericResponse
import com.devlab74.blogx.api.main.BlogxMainService
import com.devlab74.blogx.api.main.response.BlogCreateUpdateResponse
import com.devlab74.blogx.api.main.response.BlogIsAuthorResponse
import com.devlab74.blogx.api.main.response.BlogListSearchResponse
import com.devlab74.blogx.di.main.MainScope
import com.devlab74.blogx.models.AuthToken
import com.devlab74.blogx.models.BlogPost
import com.devlab74.blogx.persistence.BlogPostDao
import com.devlab74.blogx.persistence.returnOrderedBlogQuery
import com.devlab74.blogx.repository.NetworkBoundResource
import com.devlab74.blogx.repository.buildError
import com.devlab74.blogx.repository.safeApiCall
import com.devlab74.blogx.session.SessionManager
import com.devlab74.blogx.ui.main.blog.state.BlogViewState
import com.devlab74.blogx.util.*
import com.devlab74.blogx.util.ErrorHandling.Companion.handleErrors
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MultipartBody
import okhttp3.RequestBody
import timber.log.Timber
import java.lang.Exception
import javax.inject.Inject

@FlowPreview
@MainScope
class BlogRepositoryImpl
@Inject
constructor(
    val application: Application,
    val blogxMainService: BlogxMainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager
): BlogRepository {

    override fun searchBlogPosts(
        authToken: AuthToken,
        query: String,
        filterAndOrder: String,
        page: Int,
        stateEvent: StateEvent
    ): Flow<DataState<BlogViewState>> {
        return object: NetworkBoundResource<BlogListSearchResponse, List<BlogPost>, BlogViewState>(
            dispatcher = IO,
            stateEvent = stateEvent,
            apiCall = {
                blogxMainService.searchListBlogPosts(
                    authorization = authToken.authToken!!,
                    query = query,
                    ordering = filterAndOrder,
                    page = page
                )
            },
            cacheCall = {
                blogPostDao.returnOrderedBlogQuery(
                    query = query,
                    filterAndOrder = filterAndOrder,
                    page = page
                )
            },
            application = application
        ){
            override suspend fun updateCache(networkObject: BlogListSearchResponse) {
                val blogPostList = networkObject.toList()
                withContext(IO) {
                    for(blogPost in blogPostList){
                        try{
                            // Launch each insert as a separate job to be executed in parallel
                            launch {
                                Timber.d("updateLocalDb: inserting blog: $blogPost")
                                blogPostDao.insert(blogPost)
                            }
                        }catch (e: Exception){
                            Timber.d("updateLocalDb: error updating cache data on blog post with id: ${blogPost.id}. ${e.message}")
                            // Could send an error report here but I don't think it is a good idea to throw an error to the UI
                            // Since there could be many blog posts being inserted/updated.
                        }
                    }
                }
            }

            override fun handleCacheSuccess(resultObj: List<BlogPost>): DataState<BlogViewState> {

                val viewState = BlogViewState(
                    blogFields = BlogViewState.BlogFields(
                        blogList = resultObj
                    )
                )
                return DataState.data(
                    response = null,
                    data = viewState,
                    stateEvent = stateEvent
                )
            }
        }.result
    }

    override fun isAuthorOfBlogPost(
        authToken: AuthToken,
        blogId: String,
        stateEvent: StateEvent
    ) = flow {
        val apiResult = safeApiCall(IO){
            blogxMainService.isAuthorOfBlogPost(
                authorization = authToken.authToken!!,
                blogId = blogId
            )
        }
        emit(
            object: ApiResponseHandler<BlogViewState, BlogIsAuthorResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ){
                override suspend fun handleSuccess(resultObj: BlogIsAuthorResponse): DataState<BlogViewState> {
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

                    val viewState = BlogViewState(
                        viewBlogFields = BlogViewState.ViewBlogFields(
                            isAuthorOfBlogPost = false
                        )
                    )
                    return when {
                        !resultObj.isAuthorOfBlogPost!! -> {
                            DataState.data(
                                response = null,
                                data = viewState,
                                stateEvent = stateEvent
                            )
                        }

                        resultObj.isAuthorOfBlogPost!! -> {
                            viewState.viewBlogFields.isAuthorOfBlogPost = true
                            DataState.data(
                                response = null,
                                data = viewState,
                                stateEvent = stateEvent
                            )
                        }

                        else -> {
                            buildError(
                                handleErrors(9001, application),
                                UIComponentType.None,
                                stateEvent
                            )
                        }
                    }
                }
            }.getResult()
        )
    }

    override fun deleteBlogPost(
        authToken: AuthToken,
        blogPost: BlogPost,
        stateEvent: StateEvent
    ) =  flow {
        val apiResult = safeApiCall(IO){
            blogxMainService.deleteBlogPost(
                authorization = authToken.authToken!!,
                blogId = blogPost.id
            )
        }
        emit(
            object: ApiResponseHandler<BlogViewState, GenericResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ){
                override suspend fun handleSuccess(resultObj: GenericResponse): DataState<BlogViewState> {
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
                    if(resultObj.status == handleErrors(9009, application)){
                        blogPostDao.deleteBlogPost(blogPost)
                        return DataState.data(
                            response = Response(
                                message = handleErrors(resultObj.statusCode, application),
                                uiComponentType = UIComponentType.Toast,
                                messageType = MessageType.Success
                            ),
                            stateEvent = stateEvent
                        )
                    }
                    else{
                        return buildError(
                            handleErrors(9001, application),
                            UIComponentType.Dialog,
                            stateEvent
                        )
                    }
                }
            }.getResult()
        )
    }

    override fun updateBlogPost(
        authToken: AuthToken,
        blogId: String,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?,
        stateEvent: StateEvent
    ) = flow{

        val apiResult = safeApiCall(IO){
            blogxMainService.updateBlog(
                authorization = authToken.authToken!!,
                blogId = blogId,
                title = title,
                body = body,
                image = image
            )
        }
        emit(
            object: ApiResponseHandler<BlogViewState, BlogCreateUpdateResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ){
                override suspend fun handleSuccess(resultObj: BlogCreateUpdateResponse): DataState<BlogViewState> {
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

                    val updatedBlogPost = resultObj.toBlogPost()

                    blogPostDao.updateBlogPost(
                        updatedBlogPost.id,
                        updatedBlogPost.title,
                        updatedBlogPost.body,
                        updatedBlogPost.image
                    )

                    return DataState.data(
                        response = Response(
                            message = handleErrors(resultObj.statusCode, application),
                            uiComponentType = UIComponentType.Toast,
                            messageType = MessageType.Success
                        ),
                        data =  BlogViewState(
                            viewBlogFields = BlogViewState.ViewBlogFields(
                                blogPost = updatedBlogPost
                            ),
                            updatedBlogFields = BlogViewState.UpdatedBlogFields(
                                updatedBlogTitle = updatedBlogPost.title,
                                updatedBlogBody = updatedBlogPost.body,
                                updatedImageUri = null
                            )
                        ),
                        stateEvent = stateEvent
                    )
                }

            }.getResult()
        )
    }
}