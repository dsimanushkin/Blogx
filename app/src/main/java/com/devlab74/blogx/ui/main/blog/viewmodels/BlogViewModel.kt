package com.devlab74.blogx.ui.main.blog.viewmodels

import android.content.SharedPreferences
import androidx.lifecycle.LiveData
import com.bumptech.glide.RequestManager
import com.devlab74.blogx.models.BlogPost
import com.devlab74.blogx.persistence.BlogQueryUtils
import com.devlab74.blogx.repository.main.BlogRepository
import com.devlab74.blogx.session.SessionManager
import com.devlab74.blogx.ui.BaseViewModel
import com.devlab74.blogx.ui.DataState
import com.devlab74.blogx.ui.Loading
import com.devlab74.blogx.ui.main.blog.state.BlogStateEvent
import com.devlab74.blogx.ui.main.blog.state.BlogViewState
import com.devlab74.blogx.util.AbsentLiveData
import com.devlab74.blogx.util.PreferenceKeys.Companion.BLOG_FILTER
import com.devlab74.blogx.util.PreferenceKeys.Companion.BLOG_ORDER
import javax.inject.Inject

class BlogViewModel
@Inject
constructor(
    private val sessionManager: SessionManager,
    private val blogRepository: BlogRepository,
    private val sharedPreferences: SharedPreferences,
    private val editor: SharedPreferences.Editor
): BaseViewModel<BlogStateEvent, BlogViewState>() {

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
                BlogQueryUtils.BLOG_ORDER_ASC
            )
        )
    }

    override fun initNewViewState(): BlogViewState {
        return BlogViewState()
    }

    override fun handleStateEvent(stateEvent: BlogStateEvent): LiveData<DataState<BlogViewState>> {
        when(stateEvent) {
            is BlogStateEvent.BlogSearchEvent -> {
                return sessionManager.cachedToken.value?.let { authToken ->
                    blogRepository.searchBlogPost(
                        authToken = authToken,
                        query = getSearchQuery(),
                        filterAndOrder = getOrder() + getFilter(),
                        page = getPage()
                    )
                }?: AbsentLiveData.create()
            }
            is BlogStateEvent.CheckAuthorOfBlogPost -> {
                return AbsentLiveData.create()
            }
            is BlogStateEvent.None -> {
                return object : LiveData<DataState<BlogViewState>>() {
                    override fun onActive() {
                        super.onActive()
                        value = DataState(
                            null,
                            Loading(false),
                            null
                        )
                    }
                }
            }
        }
    }

    fun saveFilterOptions(filter: String, order: String) {
        editor.putString(BLOG_FILTER, filter)
        editor.apply()

        editor.putString(BLOG_ORDER, order)
        editor.apply()
    }

    fun cancelActiveJobs() {
        blogRepository.cancelActiveJobs()
        handlePendingData()
    }

    private fun handlePendingData() {
        setStateEvent(
            BlogStateEvent.None()
        )
    }

    override fun onCleared() {
        super.onCleared()
        cancelActiveJobs()
    }

}