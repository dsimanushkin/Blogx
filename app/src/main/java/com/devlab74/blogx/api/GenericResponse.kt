package com.devlab74.blogx.api

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class GenericResponse(
    @Json(name = "status")
    var status: String,

    @Json(name = "status_code")
    var statusCode: Int,

    @Json(name = "status_message")
    var statusMessage: String
)