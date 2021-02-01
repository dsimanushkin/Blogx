package com.devlab74.blogx.ui.auth

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import com.devlab74.blogx.api.auth.network_responses.LoginResponse
import com.devlab74.blogx.api.auth.network_responses.RegistrationResponse
import com.devlab74.blogx.repository.auth.AuthRepository
import com.devlab74.blogx.util.GenericApiResponse
import javax.inject.Inject

class AuthViewModel
@Inject
constructor(
    val authRepository: AuthRepository
): ViewModel() {
    fun testLogin(): LiveData<GenericApiResponse<LoginResponse>> {
        return authRepository.testLoginRequest(
            "test-username",
            "password123"
        )
    }

    fun testRegister(): LiveData<GenericApiResponse<RegistrationResponse>> {
        return authRepository.testRegistrationRequest(
            "testemail@mail.com",
            "test-username",
            "password123",
            "password123"
        )
    }
}