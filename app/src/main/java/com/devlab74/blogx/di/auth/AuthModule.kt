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

@FlowPreview
@Module
object AuthModule {

    @JvmStatic
    @AuthScope
    @Provides
    fun provideAuthApiService(retrofitBuilder: Retrofit.Builder): BlogxAuthService {
        return retrofitBuilder
            .build()
            .create(BlogxAuthService::class.java)
    }

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