package com.devlab74.blogx.repository.main

import android.app.Application
import com.devlab74.blogx.api.main.BlogxMainService
import com.devlab74.blogx.api.main.response.BlogCreateUpdateResponse
import com.devlab74.blogx.di.main.MainScope
import com.devlab74.blogx.models.AuthToken
import com.devlab74.blogx.persistence.BlogPostDao
import com.devlab74.blogx.repository.safeApiCall
import com.devlab74.blogx.session.SessionManager
import com.devlab74.blogx.ui.main.create_blog.state.CreateBlogViewState
import com.devlab74.blogx.util.*
import com.devlab74.blogx.util.ErrorHandling.Companion.handleErrors
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.flow
import okhttp3.MultipartBody
import okhttp3.RequestBody
import javax.inject.Inject

/**
 * This class is responsible for implementation of functions declared in CreateBlogRepository interface
 */

@FlowPreview
@MainScope
class CreateBlogRepositoryImpl
@Inject
constructor(
    val application: Application,
    val blogxMainService: BlogxMainService,
    val blogPostDao: BlogPostDao,
    val sessionManager: SessionManager
): CreateBlogRepository {

    override fun createNewBlogPost(
        authToken: AuthToken,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?,
        stateEvent: StateEvent
    ) = flow{

        val apiResult = safeApiCall(IO){
            blogxMainService.createBlog(
                authorization = authToken.authToken!!,
                title = title,
                body = body,
                image = image
            )
        }

        emit(
            object: ApiResponseHandler<CreateBlogViewState, BlogCreateUpdateResponse>(
                response = apiResult,
                stateEvent = stateEvent
            ){
                override suspend fun handleSuccess(resultObj: BlogCreateUpdateResponse): DataState<CreateBlogViewState> {
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
                    blogPostDao.insert(updatedBlogPost)

                    return DataState.data(
                        response = Response(
                            message = handleErrors(resultObj.statusCode, application),
                            uiComponentType = UIComponentType.Dialog,
                            messageType = MessageType.Success
                        ),
                        data = null,
                        stateEvent = stateEvent
                    )
                }
            }.getResult()
        )
    }
}