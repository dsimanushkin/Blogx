package com.devlab74.blogx.ui.main.blog.viewmodels

import android.net.Uri
import com.devlab74.blogx.models.BlogPost
import com.devlab74.blogx.persistence.BlogQueryUtils.Companion.BLOG_FILTER_DATE_UPDATED
import com.devlab74.blogx.persistence.BlogQueryUtils.Companion.BLOG_ORDER_DESC
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

/**
 * Getters file that are related to BlogViewModel
 */

@FlowPreview
@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.getSearchQuery(): String {
    return getCurrentViewStateOrNew().blogFields.searchQuery?: return ""
}

@FlowPreview
@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.getPage(): Int {
    return getCurrentViewStateOrNew().blogFields.page?: return 1
}

@FlowPreview
@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.getFilter(): String {
    return getCurrentViewStateOrNew().blogFields.filter?: BLOG_FILTER_DATE_UPDATED
}

@FlowPreview
@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.getOrder(): String {
    return getCurrentViewStateOrNew().blogFields.order?: BLOG_ORDER_DESC
}

@FlowPreview
@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.getBlogId(): String {
    getCurrentViewStateOrNew().let {
        it.viewBlogFields.blogPost?.let { blogPost ->
            return blogPost.id
        }
        return ""
    }
}

@FlowPreview
@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.isAuthorOfBlogPost(): Boolean {
    return getCurrentViewStateOrNew().viewBlogFields.isAuthorOfBlogPost?: false
}

@FlowPreview
@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.getBlogPost(): BlogPost {
    getCurrentViewStateOrNew().let {
        return it.viewBlogFields.blogPost?.let { blogPost ->
            return blogPost
        }?: getDummyBlogPost()
    }
}

@FlowPreview
@OptIn(ExperimentalCoroutinesApi::class)
fun getDummyBlogPost(): BlogPost {
    return BlogPost("null", "", "", "", 1, "")
}

@FlowPreview
@OptIn(ExperimentalCoroutinesApi::class)
fun BlogViewModel.getUpdatedBlogUri(): Uri? {
    getCurrentViewStateOrNew().let { viewState ->
        viewState.updatedBlogFields.updatedImageUri?.let {
            return it
        }
    }
    return null
}