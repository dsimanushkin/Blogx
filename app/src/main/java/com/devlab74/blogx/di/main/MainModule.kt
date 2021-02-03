package com.devlab74.blogx.di.main

import android.app.Application
import com.devlab74.blogx.api.main.BlogxMainService
import com.devlab74.blogx.persistence.AccountPropertiesDao
import com.devlab74.blogx.repository.main.AccountRepository
import com.devlab74.blogx.session.SessionManager
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
class MainModule {

    @MainScope
    @Provides
    fun provideBlogxMainService(retrofitBuilder: Retrofit.Builder): BlogxMainService {
        return retrofitBuilder
            .build()
            .create(BlogxMainService::class.java)
    }

    @MainScope
    @Provides
    fun provideMainRepository(
        application: Application,
        blogxMainService: BlogxMainService,
        accountPropertiesDao: AccountPropertiesDao,
        sessionManager: SessionManager
    ): AccountRepository {
        return AccountRepository(
            application,
            blogxMainService,
            accountPropertiesDao,
            sessionManager
        )
    }

}