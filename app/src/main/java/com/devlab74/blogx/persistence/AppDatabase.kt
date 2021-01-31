package com.devlab74.blogx.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.devlab74.blogx.models.AccountProperties
import com.devlab74.blogx.models.AuthToken

@Database(entities = [AuthToken::class, AccountProperties::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun getAuthTokenDao(): AuthTokenDao
    abstract fun getAccountPropertiesDao(): AccountPropertiesDao

    companion object {
        const val DATABASE_NAME = "blogx_app_db"
    }
}