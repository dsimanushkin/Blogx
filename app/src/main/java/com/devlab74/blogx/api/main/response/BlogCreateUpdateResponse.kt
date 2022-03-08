package com.devlab74.blogx.api.main.response

import com.devlab74.blogx.models.BlogPost
import com.devlab74.blogx.util.DateUtils
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * This class is parsing an API response responsible for Creating and Updating BlogPost
 */

@JsonClass(generateAdapter = true)
class BlogCreateUpdateResponse(
    @Json(name = "status")
    var status: String,

    @Json(name = "status_code")
    var statusCode: Int,

    @Json(name = "status_message")
    var statusMessage: String,

    @Json(name = "id")
    var id: String? = null,

    @Json(name = "title")
    var title: String? = null,

    @Json(name = "body")
    var body: String? = null,

    @Json(name = "image")
    var image: String? = null,

    @Json(name = "date_updated")
    var dateUpdated: String? = null,

    @Json(name = "username")
    var username: String? = null
) {
    // Converting API response to BlogPost model + converting server date string to long
    fun toBlogPost(): BlogPost {
        return BlogPost(
            id = id!!,
            title = title!!,
            body = body!!,
            image = image!!,
            dateUpdated = DateUtils.convertServerStringDateToLong(dateUpdated!!),
            username = username!!
        )
    }
}