package com.devlab74.blogx.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "blog_post",
    primaryKeys = ["id"]
)
@Parcelize
data class BlogPost(

    @Json(name = "id")
    @ColumnInfo(name = "id")
    var id: String,

    @Json(name = "title")
    @ColumnInfo(name = "title")
    var title: String,

    @Json(name = "body")
    @ColumnInfo(name = "body")
    var body: String,

    @Json(name = "image")
    @ColumnInfo(name = "image")
    var image: String,

    @Json(name = "date_updated")
    @ColumnInfo(name = "date_updated")
    var dateUpdated: Long,

    @Json(name = "username")
    @ColumnInfo(name = "username")
    var username: String

) : Parcelable