package com.devlab74.blogx.api.main.response

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class BlogIsAuthorResponse(
    @Json(name = "status")
    var status: String,

    @Json(name = "status_code")
    var statusCode: Int,

    @Json(name = "status_message")
    var statusMessage: String,

    @Json(name = "is_author")
    var isAuthorOfBlogPost: Boolean? = null
) {
    override fun toString(): String {
        return "BlogIsAuthorResponse(status='$status', statusCode=$statusCode, statusMessage='$statusMessage', isAuthorOfBlogPost=$isAuthorOfBlogPost)"
    }
}