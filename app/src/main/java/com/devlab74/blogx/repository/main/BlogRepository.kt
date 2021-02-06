package com.devlab74.blogx.repository.main

import com.devlab74.blogx.di.main.MainScope
import com.devlab74.blogx.models.AuthToken
import com.devlab74.blogx.models.BlogPost
import com.devlab74.blogx.ui.main.blog.state.BlogViewState
import com.devlab74.blogx.util.DataState
import com.devlab74.blogx.util.StateEvent
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.Flow
import okhttp3.MultipartBody
import okhttp3.RequestBody

@FlowPreview
@MainScope
interface BlogRepository {

    fun searchBlogPosts(
        authToken: AuthToken,
        query: String,
        filterAndOrder: String,
        page: Int,
        stateEvent: StateEvent
    ): Flow<DataState<BlogViewState>>

    fun isAuthorOfBlogPost(
        authToken: AuthToken,
        blogId: String,
        stateEvent: StateEvent
    ): Flow<DataState<BlogViewState>>

    fun deleteBlogPost(
        authToken: AuthToken,
        blogPost: BlogPost,
        stateEvent: StateEvent
    ): Flow<DataState<BlogViewState>>

    fun updateBlogPost(
        authToken: AuthToken,
        blogId: String,
        title: RequestBody,
        body: RequestBody,
        image: MultipartBody.Part?,
        stateEvent: StateEvent
    ): Flow<DataState<BlogViewState>>

}