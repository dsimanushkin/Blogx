package com.devlab74.blogx.ui.main.create_blog

import android.net.Uri
import com.devlab74.blogx.di.main.MainScope
import com.devlab74.blogx.repository.main.CreateBlogRepository
import com.devlab74.blogx.session.SessionManager
import com.devlab74.blogx.ui.BaseViewModel
import com.devlab74.blogx.ui.main.create_blog.state.CreateBlogStateEvent
import com.devlab74.blogx.ui.main.create_blog.state.CreateBlogViewState
import com.devlab74.blogx.util.*
import com.devlab74.blogx.util.ErrorHandling.Companion.INVALID_STATE_EVENT
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType
import okhttp3.RequestBody
import javax.inject.Inject

/**
* CreateBlogViewModel class is responsible for setting new State Events and for setting fields
* as well as containing method responsible for logging out user
*/

@ExperimentalCoroutinesApi
@FlowPreview
@MainScope
class CreateBlogViewModel
@Inject
constructor(
    private val createBlogRepository: CreateBlogRepository,
    val sessionManager: SessionManager
): BaseViewModel<CreateBlogViewState>() {
    override fun handleNewData(data: CreateBlogViewState) {
        setNewBlogFields(
            data.blogFields.newBlogTitle,
            data.blogFields.newBlogBody,
            data.blogFields.newImageUri
        )
    }

    override fun setStateEvent(stateEvent: StateEvent) {
        sessionManager.cachedToken.value?.let { authToken ->
            val job: Flow<DataState<CreateBlogViewState>> = when(stateEvent){
                is CreateBlogStateEvent.CreateNewBlogEvent -> {
                    val title = RequestBody.create(
                        MediaType.parse("text/plain"),
                        stateEvent.title
                    )
                    val body = RequestBody.create(
                        MediaType.parse("text/plain"),
                        stateEvent.body
                    )

                    createBlogRepository.createNewBlogPost(
                        stateEvent = stateEvent,
                        authToken = authToken,
                        title = title,
                        body = body,
                        image = stateEvent.image
                    )
                }

                else -> {
                    flow{
                        emit(
                            DataState.error(
                                response = Response(
                                    message = INVALID_STATE_EVENT,
                                    uiComponentType = UIComponentType.None,
                                    messageType = MessageType.Error
                                ),
                                stateEvent = stateEvent
                            )
                        )
                    }
                }
            }
            launchJob(stateEvent, job)
        }
    }

    override fun initNewViewState(): CreateBlogViewState {
        return CreateBlogViewState()
    }

    fun setNewBlogFields(title: String?, body: String?, uri: Uri?){
        val update = getCurrentViewStateOrNew()
        val newBlogFields = update.blogFields
        title?.let{ newBlogFields.newBlogTitle = it }
        body?.let{ newBlogFields.newBlogBody = it }
        uri?.let{ newBlogFields.newImageUri = it }
        update.blogFields = newBlogFields
        setViewState(update)
    }

    fun clearNewBlogFields(){
        val update = getCurrentViewStateOrNew()
        update.blogFields = CreateBlogViewState.NewBlogFields()
        setViewState(update)
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }
}