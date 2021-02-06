package com.devlab74.blogx.ui.auth.state

import android.app.Application
import android.os.Parcelable
import com.devlab74.blogx.R
import com.devlab74.blogx.models.AuthToken
import kotlinx.android.parcel.Parcelize

@Parcelize
data class AuthViewState(
    var registrationFields: RegistrationFields? = null,
    var loginFields: LoginFields? = null,
    var authToken: AuthToken? = null
) : Parcelable

@Parcelize
data class RegistrationFields(
    var registrationEmail: String? = null,
    var registrationUsername: String? = null,
    var registrationPassword: String? = null,
    var registrationConfirmPassword: String? = null
) : Parcelable {
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

@Parcelize
data class LoginFields(
    var loginUsername: String? = null,
    var loginPassword: String? = null
) : Parcelable {
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