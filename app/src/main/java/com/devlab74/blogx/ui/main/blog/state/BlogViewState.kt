package com.devlab74.blogx.ui.main.blog.state

import com.devlab74.blogx.models.BlogPost

data class BlogViewState(
    // BlogFragment vars
    var blogFields: BlogFields = BlogFields()

    // ViewBlogFragment vars

    // UpdateBlogFragment vars
) {

    data class BlogFields(
        var blogList: List<BlogPost> = ArrayList<BlogPost>(),
        var searchQuery: String = ""
    )

}