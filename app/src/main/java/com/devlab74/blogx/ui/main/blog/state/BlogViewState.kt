package com.devlab74.blogx.ui.main.blog.state

import android.net.Uri
import com.devlab74.blogx.models.BlogPost
import com.devlab74.blogx.persistence.BlogQueryUtils.Companion.BLOG_ORDER_ASC
import com.devlab74.blogx.persistence.BlogQueryUtils.Companion.ORDER_BY_ASC_DATE_UPDATED

data class BlogViewState(
    // BlogFragment vars
    var blogFields: BlogFields = BlogFields(),

    // ViewBlogFragment vars
    var viewBlogFields: ViewBlogFields = ViewBlogFields(),

    // UpdateBlogFragment vars
    var updatedBlogFields: UpdatedBlogFields = UpdatedBlogFields()
) {

    data class BlogFields(
        var blogList: List<BlogPost> = ArrayList<BlogPost>(),
        var searchQuery: String = "",
        var page: Int = 1,
        var isQueryInProgress: Boolean = false,
        var isQueryExhausted: Boolean = false,
        var filter: String = ORDER_BY_ASC_DATE_UPDATED,
        var order: String = BLOG_ORDER_ASC
    )

    data class ViewBlogFields(
        var blogPost: BlogPost? = null,
        var isAuthorOfBlogPost: Boolean = false
    )

    data class UpdatedBlogFields(
        var updateBlogTitle: String? = null,
        var updateBlogBody: String? = null,
        var updateImageUri: Uri? = null
    )
}