package com.devlab74.blogx.models

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.squareup.moshi.Json

@Entity(
    tableName = "account_properties",
    indices = [
        Index(value = ["id"], unique = true)
    ]
)
data class AccountProperties(

    @PrimaryKey(autoGenerate = true)
    var pk: Int,

    @Json(name = "id")
    @ColumnInfo(name = "id")
    var id: String,

    @Json(name = "email")
    @ColumnInfo(name = "email")
    var email: String,

    @Json(name = "username")
    @ColumnInfo(name = "username")
    var username: String

) {
    constructor(
        id: String,
        email: String,
        username: String
    ): this(0, id, email, username)

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