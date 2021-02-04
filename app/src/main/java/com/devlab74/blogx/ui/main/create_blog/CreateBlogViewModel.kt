package com.devlab74.blogx.ui.main.create_blog

import android.net.Uri
import androidx.lifecycle.LiveData
import androidx.lifecycle.liveData
import com.devlab74.blogx.repository.main.CreateBlogRepository
import com.devlab74.blogx.session.SessionManager
import com.devlab74.blogx.ui.BaseViewModel
import com.devlab74.blogx.ui.DataState
import com.devlab74.blogx.ui.Loading
import com.devlab74.blogx.ui.main.create_blog.state.CreateBlogStateEvent
import com.devlab74.blogx.ui.main.create_blog.state.CreateBlogViewState
import com.devlab74.blogx.util.AbsentLiveData
import okhttp3.MediaType
import okhttp3.RequestBody
import javax.inject.Inject

class CreateBlogViewModel
@Inject
constructor(
    val createBlogRepository: CreateBlogRepository,
    val sessionManager: SessionManager
): BaseViewModel<CreateBlogStateEvent, CreateBlogViewState>() {
    override fun initNewViewState(): CreateBlogViewState {
        return CreateBlogViewState()
    }

    override fun handleStateEvent(stateEvent: CreateBlogStateEvent): LiveData<DataState<CreateBlogViewState>> {
        when(stateEvent) {
            is CreateBlogStateEvent.CreateNewBlogEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    val title = RequestBody.create(
                        MediaType.parse("text/plain"),
                        stateEvent.title
                    )
                    val body = RequestBody.create(
                        MediaType.parse("text/plain"),
                        stateEvent.body
                    )
                    createBlogRepository.createNewBlogPost(
                        authToken,
                        title,
                        body,
                        stateEvent.image
                    )
                }?: AbsentLiveData.create()
            }
            is CreateBlogStateEvent.None -> {
                return liveData {
                    emit(
                        DataState(
                            null,
                            Loading(false),
                            null
                        )
                    )
                }
            }
        }
    }

    fun setNewBlogFields(
        title: String?,
        body: String?,
        uri: Uri?
    ) {
        val update = getCurrentViewStateOrNew()
        val newBlogFields = update.blogFields
        title?.let { newBlogFields.newBlogTitle = it }
        body?.let { newBlogFields.newBlogBody = it }
        uri?.let { newBlogFields.newImageUri = it }
        update.blogFields = newBlogFields
        setViewState(update)
    }

    fun getNewImageUri(): Uri? {
        getCurrentViewStateOrNew().let {
            it.blogFields.let {
                return it.newImageUri
            }
        }
    }

    fun clearNewBlogFields() {
        val update = getCurrentViewStateOrNew()
        update.blogFields = CreateBlogViewState.NewBlogFields()
        setViewState(update)
    }

    fun cancelActiveJobs() {
        createBlogRepository.cancelActiveJobs()
        handlePendingData()
    }

    private fun handlePendingData() {
        setStateEvent(CreateBlogStateEvent.None())
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }


}