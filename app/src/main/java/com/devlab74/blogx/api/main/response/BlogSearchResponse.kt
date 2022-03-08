package com.devlab74.blogx.api.main.response

import com.devlab74.blogx.models.BlogPost
import com.devlab74.blogx.util.DateUtils
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * This class is parsing an API response responsible for single Blog Search result
 */

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
    // As this is not a Data class this method needs to be overriden
    override fun toString(): String {
        return "BlogSearchResponse(id='$id', title='$title', body='$body', image='$image', dateUpdated='$dateUpdated', username='$username')"
    }

    // Converting API response to BlogPost model + converting server date string to long
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