package com.devlab74.blogx.api.main.response

import com.devlab74.blogx.models.BlogPost
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * This class is parsing an API response responsible for list of Blog Search results
 */

@JsonClass(generateAdapter = true)
class BlogListSearchResponse(

    @Json(name = "status")
    var status: String,

    @Json(name = "status_code")
    var statusCode: Int,

    @Json(name = "status_message")
    var statusMessage: String,

    @Json(name = "results")
    var results: List<BlogSearchResponse>? = null

) {
    // As this is not a Data class this method needs to be overriden
    override fun toString(): String {
        return "BlogListSearchResponse(status='$status', statusCode=$statusCode, statusMessage='$statusMessage', results=$results)"
    }

    // Converting API response list to list of BlogPosts
    fun toList(): List<BlogPost> {
        val blogPostList: ArrayList<BlogPost> = ArrayList()
        if (results != null) {
            for (blogPostResponse in results!!) {
                blogPostList.add(
                    blogPostResponse.toBlogPost()
                )
            }
        }
        return blogPostList
    }
}