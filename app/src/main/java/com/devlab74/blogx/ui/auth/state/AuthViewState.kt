package com.devlab74.blogx.ui.auth.state

import android.app.Application
import com.devlab74.blogx.R
import com.devlab74.blogx.models.AuthToken

data class AuthViewState(
    var registrationFields: RegistrationFields? = RegistrationFields(),
    var loginFields: LoginFields? = LoginFields(),
    var authToken: AuthToken? = null
)

data class RegistrationFields(
    var registrationEmail: String? = null,
    var registrationUsername: String? = null,
    var registrationPassword: String? = null,
    var registrationConfirmPassword: String? = null
) {
    class RegistrationError() {
        companion object {
            fun mustFillAllFields(application: Application): String {
                return application.getString(R.string.registration_error_must_fill_all_fields)
            }

            fun passwordsDoNotMatch(application: Application): String {
                return application.getString(R.string.registration_error_password_must_match)
            }

            fun none(application: Application): String {
                return application.getString(R.string.registration_error_none)
            }
        }
    }

    fun isValidForRegistration(application: Application): String {
        if (registrationEmail.isNullOrEmpty() || registrationUsername.isNullOrEmpty() || registrationPassword.isNullOrEmpty() || registrationConfirmPassword.isNullOrEmpty()) {
            return RegistrationError.mustFillAllFields(application)
        }

        if (registrationPassword != registrationConfirmPassword) {
            return RegistrationError.passwordsDoNotMatch(application)
        }

        return RegistrationError.none(application)
    }
}

data class LoginFields(
    var loginUsername: String? = null,
    var loginPassword: String? = null
) {
    class LoginError {
        companion object {
            fun mustFillAllFields(application: Application): String {
                return application.getString(R.string.login_error_must_fill_all_fields)
            }

            fun none(application: Application): String {
                return application.getString(R.string.login_error_none)
            }
        }
    }

    fun isValidForLogin(application: Application): String {
        if (loginUsername.isNullOrEmpty() || loginPassword.isNullOrEmpty()) {
            return LoginError.mustFillAllFields(application)
        }
        return LoginError.none(application)
    }
}