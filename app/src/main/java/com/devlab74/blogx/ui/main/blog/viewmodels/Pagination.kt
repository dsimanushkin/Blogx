package com.devlab74.blogx.ui.main.blog.viewmodels

import com.devlab74.blogx.ui.main.blog.state.BlogStateEvent
import com.devlab74.blogx.ui.main.blog.state.BlogViewState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import timber.log.Timber

/**
 * File with functions that are responsible for Pagination and related to BlogViewModel
 */

@FlowPreview
@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.resetPage() {
    val update = getCurrentViewStateOrNew()
    update.blogFields.page = 1
    setViewState(update)
}

@FlowPreview
@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.loadFirstPage() {
    if (!isJobAlreadyActive(BlogStateEvent.BlogSearchEvent())) {
        setQueryExhausted(false)
        resetPage()
        setStateEvent(BlogStateEvent.BlogSearchEvent())
    }
}

@FlowPreview
@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.incrementPageNumber() {
    val update = getCurrentViewStateOrNew()
    val page = update.copy().blogFields.page?: 1
    update.blogFields.page = page.plus(1)
    setViewState(update)
}

@FlowPreview
@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.nextPage() {
    if (!isJobAlreadyActive(BlogStateEvent.BlogSearchEvent()) && !viewState.value!!.blogFields.isQueryExhausted!!) {
        Timber.d("BlogViewModel: Attempting to load next page...")
        incrementPageNumber()
        setStateEvent(BlogStateEvent.BlogSearchEvent())
    }
}

@FlowPreview
@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.handleIncomingBlogListData(viewState: BlogViewState) {
    viewState.blogFields.let { blogFields ->
        blogFields.blogList?.let { setBlogListData(it) }
    }
}

@FlowPreview
@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.refreshFromCache() {
    if (!isJobAlreadyActive(BlogStateEvent.BlogSearchEvent())) {
        setQueryExhausted(false)
        setStateEvent(BlogStateEvent.BlogSearchEvent(false))
    }
}