package com.devlab74.blogx.api.auth

import com.devlab74.blogx.api.auth.network_responses.LoginResponse
import com.devlab74.blogx.api.auth.network_responses.RegistrationResponse
import com.devlab74.blogx.di.auth.AuthScope
import com.devlab74.blogx.util.Constants.Companion.API_ACCESS_TOKEN
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

/**
 * This interface holding API methods that is responsible to make calls that are specifically
 * related to LOGIN part in the app
 */

@AuthScope
interface BlogxAuthService {
    // Login request
    @POST("account/login")
    @FormUrlEncoded
    suspend fun login(
        @Header("api-access-token") apiAccessToken: String? = API_ACCESS_TOKEN,
        @Field("username") username: String,
        @Field("password") password: String
    ): LoginResponse

    // Register request
    @POST("account/register")
    @FormUrlEncoded
    suspend fun register(
        @Header("api-access-token") apiAccessToken: String? = API_ACCESS_TOKEN,
        @Field("email") email: String,
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("confirm_password") confirmPassword: String
    ): RegistrationResponse
}