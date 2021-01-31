package com.devlab74.blogx.ui.auth

import androidx.lifecycle.ViewModel
import com.devlab74.blogx.repository.auth.AuthRepository
import javax.inject.Inject

class AuthViewModel
@Inject
constructor(
    val authRepository: AuthRepository
): ViewModel() {

}