package com.devlab74.blogx.api.main

import androidx.lifecycle.LiveData
import com.devlab74.blogx.api.GenericResponse
import com.devlab74.blogx.api.main.response.BlogCreateUpdateResponse
import com.devlab74.blogx.api.main.response.BlogIsAuthorResponse
import com.devlab74.blogx.api.main.response.BlogListSearchResponse
import com.devlab74.blogx.models.AccountProperties
import com.devlab74.blogx.util.Constants
import com.devlab74.blogx.util.GenericApiResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

interface BlogxMainService {

    @GET("account/properties")
    fun getAccountProperties(
        @Header("api-access-token") apiAccessToken: String? = Constants.API_ACCESS_TOKEN,
        @Header("auth-token") authorization: String
    ): LiveData<GenericApiResponse<AccountProperties>>

    @PUT("account/properties/update")
    @FormUrlEncoded
    fun saveAccountProperties(
        @Header("api-access-token") apiAccessToken: String? = Constants.API_ACCESS_TOKEN,
        @Header("auth-token") authorization: String,
        @Field("email") email: String,
        @Field("username") username: String
    ): LiveData<GenericApiResponse<GenericResponse>>

    @PUT("account/change-password")
    @FormUrlEncoded
    fun updatePassword(
        @Header("api-access-token") apiAccessToken: String? = Constants.API_ACCESS_TOKEN,
        @Header("auth-token") authorization: String,
        @Field("current_password") currentPassword: String,
        @Field("new_password") newPassword: String,
        @Field("confirm_new_password") confirmNewPassword: String
    ): LiveData<GenericApiResponse<GenericResponse>>

    @GET("blog/list")
    fun searchListBlogPosts(
        @Header("api-access-token") apiAccessToken: String? = Constants.API_ACCESS_TOKEN,
        @Header("auth-token") authorization: String,
        @Query("search") query: String,
        @Query("ordering") ordering: String,
        @Query("page") page: Int
    ): LiveData<GenericApiResponse<BlogListSearchResponse>>

    @GET("blog/{blogId}/is-author")
    fun isAuthorOfBlogPost(
        @Header("api-access-token") apiAccessToken: String? = Constants.API_ACCESS_TOKEN,
        @Header("auth-token") authorization: String,
        @Path("blogId") blogId: String
    ): LiveData<GenericApiResponse<BlogIsAuthorResponse>>

    @DELETE("blog/{blogId}/delete")
    fun deleteBlogPost(
        @Header("api-access-token") apiAccessToken: String? = Constants.API_ACCESS_TOKEN,
        @Header("auth-token") authorization: String,
        @Path("blogId") blogId: String
    ): LiveData<GenericApiResponse<GenericResponse>>

    @Multipart
    @PUT("blog/{blogId}/update")
    fun updateBlog(
        @Header("api-access-token") apiAccessToken: String? = Constants.API_ACCESS_TOKEN,
        @Header("auth-token") authorization: String,
        @Path("blogId") blogId: String,
        @Part("title") title: RequestBody,
        @Part("body") body: RequestBody,
        @Part image: MultipartBody.Part?
    ): LiveData<GenericApiResponse<BlogCreateUpdateResponse>>
}