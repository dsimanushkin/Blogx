package com.devlab74.blogx.api.auth.network_responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class LoginResponse(
    @Json(name = "status")
    var status: String,

    @Json(name = "status_code")
    var statusCode: Int,

    @Json(name = "status_message")
    var statusMessage: String,

    @Json(name = "id")
    var id: String? = null,

    @Json(name = "email")
    var email: String? = null,

    @Json(name = "auth_token")
    var authToken: String? = null
) {
    override fun toString(): String {
        return "LoginResponse(status='$status', statusCode=$statusCode, statusMessage='$statusMessage', id='$id', email='$email', authToken='$authToken')"
    }
}