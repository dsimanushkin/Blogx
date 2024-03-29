package com.devlab74.blogx.persistence

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.devlab74.blogx.models.AuthToken

/**
 * DAO interface is responsible for all DB operations related to AuthToken
 */

@Dao
interface AuthTokenDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(authToken: AuthToken): Long

    @Query("UPDATE auth_token SET auth_token = null WHERE account_id = :id")
    suspend fun nullifyToken(id: String): Int

    @Query("SELECT * FROM auth_token WHERE account_id = :id")
    suspend fun searchById(id: String): AuthToken?

}