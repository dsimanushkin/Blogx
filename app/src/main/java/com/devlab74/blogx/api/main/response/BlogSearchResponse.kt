package com.devlab74.blogx.api.main.response

import com.devlab74.blogx.models.BlogPost
import com.devlab74.blogx.util.DateUtils
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
class BlogSearchResponse (

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
) {

    override fun toString(): String {
        return "BlogSearchResponse(id='$id', title='$title', body='$body', image='$image', dateUpdated='$dateUpdated', username='$username')"
    }

    fun toBlogPost(): BlogPost {
        return BlogPost(
            id = id,
            title = title,
            body = body,
            image = image,
            dateUpdated = DateUtils.convertServerStringDateToLong(dateUpdated),
            username = username
        )
    }
}