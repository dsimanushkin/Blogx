package com.devlab74.blogx.di.auth

import android.app.Application
import android.content.SharedPreferences
import com.devlab74.blogx.api.auth.BlogxAuthService
import com.devlab74.blogx.persistence.AccountPropertiesDao
import com.devlab74.blogx.persistence.AuthTokenDao
import com.devlab74.blogx.repository.auth.AuthRepository
import com.devlab74.blogx.repository.auth.AuthRepositoryImpl
import com.devlab74.blogx.session.SessionManager
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.FlowPreview
import retrofit2.Retrofit

/**
 * This class is responsible to provide instances of retrofit and Auth repository
 */

@FlowPreview
@Module
object AuthModule {

    // Providing instance of Retrofit Builder (AuthScope)
    @JvmStatic
    @AuthScope
    @Provides
    fun provideAuthApiService(retrofitBuilder: Retrofit.Builder): BlogxAuthService {
        return retrofitBuilder
            .build()
            .create(BlogxAuthService::class.java)
    }

    // Providing instance of Auth Repository (AuthScope)
    @JvmStatic
    @AuthScope
    @Provides
    fun provideAuthRepository(
        application: Application,
        sessionManager: SessionManager,
        authTokenDao: AuthTokenDao,
        accountPropertiesDao: AccountPropertiesDao,
        blogxAuthService: BlogxAuthService,
        sharedPreferences: SharedPreferences,
        editor: SharedPreferences.Editor
    ): AuthRepository {
        return AuthRepositoryImpl(
            application,
            authTokenDao,
            accountPropertiesDao,
            blogxAuthService,
            sessionManager,
            sharedPreferences,
            editor
        )
    }
}