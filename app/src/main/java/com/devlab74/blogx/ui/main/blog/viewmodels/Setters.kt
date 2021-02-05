package com.devlab74.blogx.ui.main.blog.viewmodels

import android.net.Uri
import android.os.Parcelable
import com.devlab74.blogx.models.BlogPost

fun BlogViewModel.setQuery(query: String) {
    val update = getCurrentViewStateOrNew()
    update.blogFields.searchQuery = query
    setViewState(update)
}

fun BlogViewModel.setBlogListData(blogList: List<BlogPost>) {
    val update = getCurrentViewStateOrNew()
    update.blogFields.blogList = blogList
    setViewState(update)
}

fun BlogViewModel.setBlogPost(blogPost: BlogPost) {
    val update = getCurrentViewStateOrNew()
    update.viewBlogFields.blogPost = blogPost
    setViewState(update)
}

fun BlogViewModel.setIsAuthorOfBlogPost(isAuthorOfBlogPost: Boolean) {
    val update = getCurrentViewStateOrNew()
    update.viewBlogFields.isAuthorOfBlogPost = isAuthorOfBlogPost
    setViewState(update)
}

fun BlogViewModel.setQueryExhausted(isExhausted: Boolean) {
    val update = getCurrentViewStateOrNew()
    update.blogFields.isQueryExhausted = isExhausted
    setViewState(update)
}

fun BlogViewModel.setQueryInProgress(inProgress: Boolean) {
    val update = getCurrentViewStateOrNew()
    update.blogFields.isQueryInProgress = inProgress
    setViewState(update)
}

fun BlogViewModel.setBlogFilter(filter: String?) {
    filter?.let {
        val update = getCurrentViewStateOrNew()
        update.blogFields.filter = filter
        setViewState(update)
    }
}

fun BlogViewModel.setBlogOrder(order: String?) {
    order?.let {
        val update = getCurrentViewStateOrNew()
        update.blogFields.order = order
        setViewState(update)
    }
}

fun BlogViewModel.removeDeletedBlogPost() {
    val update = getCurrentViewStateOrNew()
    val list = update.blogFields.blogList.toMutableList()
    for (i in 0 until list.size) {
        if (list[i] == getBlogPost()) {
            list.remove(getBlogPost())
            break
        }
    }
    setBlogListData(list)
}

fun BlogViewModel.setUpdatedBlogFields(
    title: String?,
    body: String?,
    uri: Uri?
) {
    val update = getCurrentViewStateOrNew()
    val updatedBlogFields = update.updatedBlogFields
    title?.let { updatedBlogFields.updateBlogTitle = it }
    body?.let { updatedBlogFields.updateBlogBody = it }
    uri?.let { updatedBlogFields.updateImageUri = it }
    update.updatedBlogFields = updatedBlogFields
    setViewState(update)
}

fun BlogViewModel.updateListItem(newBlogPost: BlogPost) {
    val update = getCurrentViewStateOrNew()
    val list = update.blogFields.blogList.toMutableList()
    for (i in 0 until list.size) {
        if (list[i].id == newBlogPost.id) {
            list[i] = newBlogPost
            break
        }
    }
    update.blogFields.blogList = list
    setViewState(update)
}

fun BlogViewModel.onBlogPostUpdateSuccess(blogPost: BlogPost) {
    setUpdatedBlogFields(
        uri = null,
        title = blogPost.title,
        body = blogPost.body
    ) // Update UpdateBlogFragment
    setBlogPost(blogPost) // Update ViewBlogFragment
    updateListItem(blogPost) // Update BlogFragment
}

fun BlogViewModel.setLayoutManagerState(layoutManagerState: Parcelable) {
    val update = getCurrentViewStateOrNew()
    update.blogFields.layoutManagerState = layoutManagerState
    setViewState(update)
}

fun BlogViewModel.clearLayoutManagerState() {
    val update = getCurrentViewStateOrNew()
    update.blogFields.layoutManagerState = null
    setViewState(update)
}