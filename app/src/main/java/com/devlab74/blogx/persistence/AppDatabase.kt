package com.devlab74.blogx.persistence

import androidx.room.Database
import androidx.room.RoomDatabase
import com.devlab74.blogx.models.AccountProperties
import com.devlab74.blogx.models.AuthToken
import com.devlab74.blogx.models.BlogPost

/**
 * This class is the starting point for the DB describing entities and DAOs
 */

@Database(entities = [AuthToken::class, AccountProperties::class, BlogPost::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun getAuthTokenDao(): AuthTokenDao
    abstract fun getAccountPropertiesDao(): AccountPropertiesDao
    abstract fun getBlogPostDao(): BlogPostDao

    companion object {
        const val DATABASE_NAME = "blogx_app_db"
    }
}