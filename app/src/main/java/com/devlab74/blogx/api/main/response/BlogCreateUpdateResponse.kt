package com.devlab74.blogx.api.main.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class BlogCreateUpdateResponse(
    @Json(name = "status")
    var status: String,

    @Json(name = "status_code")
    var statusCode: Int,

    @Json(name = "status_message")
    var statusMessage: String,

    @Json(name = "id")
    var id: String,

    @Json(name = "title")
    var title: String,

    @Json(name = "body")
    var body: String,

    @Json(name = "image")
    var image: String,

    @Json(name = "date_updated")
    var dateUpdated: String,

    @Json(name = "username")
    var username: String
)