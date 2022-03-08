package com.devlab74.blogx.api.auth.network_responses

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * This class is parsing an API Registration response
 */

@JsonClass(generateAdapter = true)
class RegistrationResponse(
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

    @Json(name = "username")
    var username: String? = null,

    @Json(name = "auth_token")
    var authToken: String? = null
) {
    // As this is not a Data class this method needs to be overriden
    override fun toString(): String {
        return "RegistrationResponse(status='$status', statusCode=$statusCode, statusMessage='$statusMessage', id='$id', email='$email', username='$username', authToken='$authToken')"
    }
}