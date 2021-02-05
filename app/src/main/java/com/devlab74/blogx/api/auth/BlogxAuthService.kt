package com.devlab74.blogx.api.auth

import androidx.lifecycle.LiveData
import com.devlab74.blogx.api.auth.network_responses.LoginResponse
import com.devlab74.blogx.api.auth.network_responses.RegistrationResponse
import com.devlab74.blogx.di.auth.AuthScope
import com.devlab74.blogx.util.Constants.Companion.API_ACCESS_TOKEN
import com.devlab74.blogx.util.GenericApiResponse
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.Header
import retrofit2.http.POST

@AuthScope
interface BlogxAuthService {
    @POST("account/login")
    @FormUrlEncoded
    fun login(
        @Header("api-access-token") apiAccessToken: String? = API_ACCESS_TOKEN,
        @Field("username") username: String,
        @Field("password") password: String
    ): LiveData<GenericApiResponse<LoginResponse>>

    @POST("account/register")
    @FormUrlEncoded
    fun register(
        @Header("api-access-token") apiAccessToken: String? = API_ACCESS_TOKEN,
        @Field("email") email: String,
        @Field("username") username: String,
        @Field("password") password: String,
        @Field("confirm_password") confirmPassword: String
    ): LiveData<GenericApiResponse<RegistrationResponse>>
}