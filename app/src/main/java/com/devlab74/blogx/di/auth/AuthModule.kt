package com.devlab74.blogx.di.auth

import com.devlab74.blogx.api.auth.BlogxAuthService
import com.devlab74.blogx.persistence.AccountPropertiesDao
import com.devlab74.blogx.persistence.AuthTokenDao
import com.devlab74.blogx.repository.auth.AuthRepository
import com.devlab74.blogx.session.SessionManager
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class AuthModule {
    // Temp
    @AuthScope
    @Provides
    fun provideAuthApiService(retrofitBuilder: Retrofit.Builder): BlogxAuthService {
        return retrofitBuilder
            .build()
            .create(BlogxAuthService::class.java)
    }

    @AuthScope
    @Provides
    fun provideAuthRepository(
        sessionManager: SessionManager,
        authTokenDao: AuthTokenDao,
        accountPropertiesDao: AccountPropertiesDao,
        blogxAuthService: BlogxAuthService
    ): AuthRepository {
        return AuthRepository(
            authTokenDao,
            accountPropertiesDao,
            blogxAuthService,
            sessionManager
        )
    }
}