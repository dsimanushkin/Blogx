package com.devlab74.blogx.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "account_properties",
    indices = [
        Index(value = ["id"], unique = true)
    ],
    primaryKeys = ["id"]
)
@Parcelize
data class AccountProperties(

    @Json(name = "id")
    @ColumnInfo(name = "id")
    var id: String,

    @Json(name = "email")
    @ColumnInfo(name = "email")
    var email: String,

    @Json(name = "username")
    @ColumnInfo(name = "username")
    var username: String

) : Parcelable {

    override fun equals(other: Any?): Boolean {
        if (javaClass != other?.javaClass) {
            return false
        }

        other as AccountProperties

        if (id != other.id) return false
        if (username != other.username) return false
        if (email != other.email) return false

        return true
    }
}