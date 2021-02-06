package com.devlab74.blogx.api.main

import com.devlab74.blogx.api.GenericResponse
import com.devlab74.blogx.api.main.response.BlogCreateUpdateResponse
import com.devlab74.blogx.api.main.response.BlogIsAuthorResponse
import com.devlab74.blogx.api.main.response.BlogListSearchResponse
import com.devlab74.blogx.di.main.MainScope
import com.devlab74.blogx.models.AccountProperties
import com.devlab74.blogx.util.Constants
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.http.*

@MainScope
interface BlogxMainService {

    @GET("account/properties")
    suspend fun getAccountProperties(
        @Header("api-access-token") apiAccessToken: String? = Constants.API_ACCESS_TOKEN,
        @Header("auth-token") authorization: String
    ): AccountProperties

    @PUT("account/properties/update")
    @FormUrlEncoded
    suspend fun saveAccountProperties(
        @Header("api-access-token") apiAccessToken: String? = Constants.API_ACCESS_TOKEN,
        @Header("auth-token") authorization: String,
        @Field("email") email: String,
        @Field("username") username: String
    ): GenericResponse

    @PUT("account/change-password")
    @FormUrlEncoded
    suspend fun updatePassword(
        @Header("api-access-token") apiAccessToken: String? = Constants.API_ACCESS_TOKEN,
        @Header("auth-token") authorization: String,
        @Field("current_password") currentPassword: String,
        @Field("new_password") newPassword: String,
        @Field("confirm_new_password") confirmNewPassword: String
    ): GenericResponse

    @GET("blog/list")
    suspend fun searchListBlogPosts(
        @Header("api-access-token") apiAccessToken: String? = Constants.API_ACCESS_TOKEN,
        @Header("auth-token") authorization: String,
        @Query("search") query: String,
        @Query("ordering") ordering: String,
        @Query("page") page: Int
    ): BlogListSearchResponse

    @GET("blog/{blogId}/is-author")
    suspend fun isAuthorOfBlogPost(
        @Header("api-access-token") apiAccessToken: String? = Constants.API_ACCESS_TOKEN,
        @Header("auth-token") authorization: String,
        @Path("blogId") blogId: String
    ): BlogIsAuthorResponse

    @DELETE("blog/{blogId}/delete")
    suspend fun deleteBlogPost(
        @Header("api-access-token") apiAccessToken: String? = Constants.API_ACCESS_TOKEN,
        @Header("auth-token") authorization: String,
        @Path("blogId") blogId: String
    ): GenericResponse

    @Multipart
    @PUT("blog/{blogId}/update")
    suspend fun updateBlog(
        @Header("api-access-token") apiAccessToken: String? = Constants.API_ACCESS_TOKEN,
        @Header("auth-token") authorization: String,
        @Path("blogId") blogId: String,
        @Part("title") title: RequestBody,
        @Part("body") body: RequestBody,
        @Part image: MultipartBody.Part?
    ): BlogCreateUpdateResponse

    @Multipart
    @POST("blog/create")
    suspend fun createBlog(
        @Header("api-access-token") apiAccessToken: String? = Constants.API_ACCESS_TOKEN,
        @Header("auth-token") authorization: String,
        @Part("title") title: RequestBody,
        @Part("body") body: RequestBody,
        @Part image: MultipartBody.Part?
    ): BlogCreateUpdateResponse
}