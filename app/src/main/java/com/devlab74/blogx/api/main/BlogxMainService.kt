package com.devlab74.blogx.api.main

import androidx.lifecycle.LiveData
import com.devlab74.blogx.api.GenericResponse
import com.devlab74.blogx.models.AccountProperties
import com.devlab74.blogx.util.Constants
import com.devlab74.blogx.util.GenericApiResponse
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

}