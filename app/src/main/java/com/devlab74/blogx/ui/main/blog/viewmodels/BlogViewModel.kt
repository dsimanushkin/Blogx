package com.devlab74.blogx.ui.main.blog.viewmodels

import android.content.SharedPreferences
import com.devlab74.blogx.di.main.MainScope
import com.devlab74.blogx.persistence.BlogQueryUtils
import com.devlab74.blogx.repository.main.BlogRepository
import com.devlab74.blogx.session.SessionManager
import com.devlab74.blogx.ui.BaseViewModel
import com.devlab74.blogx.ui.main.blog.state.BlogStateEvent
import com.devlab74.blogx.ui.main.blog.state.BlogViewState
import com.devlab74.blogx.util.*
import com.devlab74.blogx.util.ErrorHandling.Companion.INVALID_STATE_EVENT
import com.devlab74.blogx.util.PreferenceKeys.Companion.BLOG_FILTER
import com.devlab74.blogx.util.PreferenceKeys.Companion.BLOG_ORDER
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import okhttp3.MediaType
import okhttp3.RequestBody
import javax.inject.Inject

/**
 * AuthViewModel class is responsible for setting new State Events and for setting fields
 */

@ExperimentalCoroutinesApi
@FlowPreview
@MainScope
class BlogViewModel
@Inject
constructor(
    private val sessionManager: SessionManager,
    private val blogRepository: BlogRepository,
    sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor
): BaseViewModel<BlogViewState>() {

    init {
        setBlogFilter(
            sharedPreferences.getString(
                BLOG_FILTER,
                BlogQueryUtils.BLOG_FILTER_DATE_UPDATED
            )
        )

        setBlogOrder(
            sharedPreferences.getString(
                BLOG_ORDER,
                BlogQueryUtils.BLOG_ORDER_DESC
            )
        )
    }

    override fun handleNewData(data: BlogViewState) {
        data.blogFields.let { blogFields ->
            blogFields.blogList?.let {
                handleIncomingBlogListData(data)
            }

            blogFields.isQueryExhausted?.let { isQueryExhausted ->
                setQueryExhausted(isQueryExhausted)
            }
        }

        data.viewBlogFields.let { viewBlogFields ->
            viewBlogFields.blogPost?.let { blogPost ->
                setBlogPost(blogPost)
            }

            viewBlogFields.isAuthorOfBlogPost?.let { isAuthor ->
                setIsAuthorOfBlogPost(isAuthor)
            }
        }

        data.updatedBlogFields.let { updatedBlogFields ->
            updatedBlogFields.updatedImageUri?.let { uri ->
                setUpdatedUri(uri)
            }

            updatedBlogFields.updatedBlogTitle?.let { title ->
                setUpdatedTitle(title)
            }

            updatedBlogFields.updatedBlogBody?.let { body ->
                setUpdatedBody(body)
            }
        }
    }

    override fun setStateEvent(stateEvent: StateEvent) {
        if(!isJobAlreadyActive(stateEvent)){
            sessionManager.cachedToken.value?.let { authToken ->
                val job: Flow<DataState<BlogViewState>> = when(stateEvent){
                    is BlogStateEvent.BlogSearchEvent -> {
                        if(stateEvent.clearLayoutManagerState){
                            clearLayoutManagerState()
                        }
                        blogRepository.searchBlogPosts(
                            stateEvent = stateEvent,
                            authToken = authToken,
                            query = getSearchQuery(),
                            filterAndOrder = getOrder() + getFilter(),
                            page = getPage()
                        )
                    }

                    is BlogStateEvent.CheckAuthorOfBlogPost -> {
                        blogRepository.isAuthorOfBlogPost(
                            stateEvent = stateEvent,
                            authToken = authToken,
                            blogId = getBlogId()
                        )
                    }

                    is BlogStateEvent.DeleteBlogPostEvent -> {
                        blogRepository.deleteBlogPost(
                            stateEvent = stateEvent,
                            authToken = authToken,
                            blogPost = getBlogPost()
                        )
                    }

                    is BlogStateEvent.UpdatedBlogPostEvent -> {
                        val title = RequestBody.create(
                            MediaType.parse("text/plain"),
                            stateEvent.title
                        )
                        val body = RequestBody.create(
                            MediaType.parse("text/plain"),
                            stateEvent.body
                        )

                        blogRepository.updateBlogPost(
                            stateEvent = stateEvent,
                            authToken = authToken,
                            blogId = getBlogId(),
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
    }

    override fun initNewViewState(): BlogViewState {
        return BlogViewState()
    }

    fun saveFilterOptions(filter: String, order: String){
        editor.putString(BLOG_FILTER, filter)
        editor.apply()

        editor.putString(BLOG_ORDER, order)
        editor.apply()
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }
}