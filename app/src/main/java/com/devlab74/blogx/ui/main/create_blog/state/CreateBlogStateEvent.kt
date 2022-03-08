package com.devlab74.blogx.ui.main.create_blog.state

import com.devlab74.blogx.util.StateEvent
import okhttp3.MultipartBody

/**
 * This sealed class is holding states when calling API/Cache related to CreateBlog -> Account part
 */

sealed class CreateBlogStateEvent: StateEvent {
    data class CreateNewBlogEvent(
        val title: String,
        val body: String,
        val image: MultipartBody.Part
    ): CreateBlogStateEvent() {
        override fun errorInfo(): String {
            return "Unable to create a new blog post."
        }

        override fun toString(): String {
            return "CreateBlogStateEvent"
        }
    }

    object None : CreateBlogStateEvent() {
        override fun errorInfo(): String {
            return "None"
        }

        override fun toString(): String {
            return "None"
        }
    }
}