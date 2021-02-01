package com.devlab74.blogx.repository.auth

import androidx.lifecycle.LiveData
import com.devlab74.blogx.api.auth.BlogxAuthService
import com.devlab74.blogx.api.auth.network_responses.LoginResponse
import com.devlab74.blogx.api.auth.network_responses.RegistrationResponse
import com.devlab74.blogx.persistence.AccountPropertiesDao
import com.devlab74.blogx.persistence.AuthTokenDao
import com.devlab74.blogx.session.SessionManager
import com.devlab74.blogx.util.GenericApiResponse

class AuthRepository
constructor(
    val authTokenDao: AuthTokenDao,
    val accountPropertiesDao: AccountPropertiesDao,
    val blogxAuthService: BlogxAuthService,
    val sessionManager: SessionManager
){
    fun testLoginRequest(
        username: String,
        password: String
    ): LiveData<GenericApiResponse<LoginResponse>> {
        return blogxAuthService.login(
            username = username,
            password = password
        )
    }

    fun testRegistrationRequest(
        email: String,
        username: String,
        password: String,
        confirmPassword: String
    ): LiveData<GenericApiResponse<RegistrationResponse>> {
        return blogxAuthService.register(
            email = email,
            username = username,
            password = password,
            confirmPassword = confirmPassword
        )
    }
}