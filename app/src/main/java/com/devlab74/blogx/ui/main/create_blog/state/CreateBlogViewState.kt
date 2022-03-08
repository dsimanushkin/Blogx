package com.devlab74.blogx.ui.main.create_blog.state

import android.net.Uri
import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

/**
 * This class describes fields that CreateBlog part will hold
 */

const val CREATE_BLOG_VIEW_STATE_BUNDLE_KEY = "com.devlab74.blogx.ui.main.create_blog.state.CreateBlogViewState"

@Parcelize
data class CreateBlogViewState(
    // CreateBlogFragment vars
    var blogFields: NewBlogFields = NewBlogFields()
) : Parcelable {

    @Parcelize
    data class NewBlogFields(
        var newBlogTitle: String? = null,
        var newBlogBody: String? = null,
        var newImageUri: Uri? = null
    ) : Parcelable

}