package com.devlab74.blogx.models

import android.os.Parcelable
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.CASCADE
import androidx.room.PrimaryKey
import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass
import kotlinx.android.parcel.Parcelize

/**
 * AuthToken data class (SQLite, Room Persistence, Foreign Key, Multiple Database Tables)
 */

const val AUTH_TOKEN_BUNDLE_KEY = "com.devlab74.blogx.models.AuthToken"

@JsonClass(generateAdapter = true)
@Entity(
    tableName = "auth_token",
    foreignKeys = [
        ForeignKey(
            entity = AccountProperties::class,
            parentColumns = ["id"],
            childColumns = ["account_id"],
            onDelete = CASCADE
        )
    ]
)
@Parcelize
data class AuthToken(
    @PrimaryKey
    @ColumnInfo(name = "account_id")
    var accountId: String = "",

    @Json(name = "auth_token")
    @ColumnInfo(name = "auth_token")
    var authToken: String? = null
) : Parcelable