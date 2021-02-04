package com.devlab74.blogx.ui.main.blog.state

sealed class BlogStateEvent {
    class BlogSearchEvent: BlogStateEvent()

    class CheckAuthorOfBlogPost: BlogStateEvent()

    class None: BlogStateEvent()
}