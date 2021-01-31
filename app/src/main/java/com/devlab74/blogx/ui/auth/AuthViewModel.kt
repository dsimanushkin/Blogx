package com.devlab74.blogx.ui.auth

import androidx.lifecycle.ViewModel
import com.devlab74.blogx.repository.auth.AuthRepository

class AuthViewModel
constructor(
    val authRepository: AuthRepository
): ViewModel() {

}