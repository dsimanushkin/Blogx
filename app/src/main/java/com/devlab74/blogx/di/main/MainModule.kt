package com.devlab74.blogx.di.main

import android.app.Application
import com.devlab74.blogx.api.main.BlogxMainService
import com.devlab74.blogx.persistence.AccountPropertiesDao
import com.devlab74.blogx.persistence.AppDatabase
import com.devlab74.blogx.persistence.BlogPostDao
import com.devlab74.blogx.repository.main.AccountRepository
import com.devlab74.blogx.repository.main.BlogRepository
import com.devlab74.blogx.repository.main.CreateBlogRepository
import com.devlab74.blogx.session.SessionManager
import dagger.Module
import dagger.Provides
import retrofit2.Retrofit

@Module
object MainModule {

    @JvmStatic
    @MainScope
    @Provides
    fun provideBlogxMainService(retrofitBuilder: Retrofit.Builder): BlogxMainService {
        return retrofitBuilder
            .build()
            .create(BlogxMainService::class.java)
    }

    @JvmStatic
    @MainScope
    @Provides
    fun provideAccountRepository(
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

    @JvmStatic
    @MainScope
    @Provides
    fun provideBlogPostDao(db: AppDatabase): BlogPostDao {
        return db.getBlogPostDao()
    }

    @JvmStatic
    @MainScope
    @Provides
    fun provideBlogRepository(
        application: Application,
        blogxMainService: BlogxMainService,
        blogPostDao: BlogPostDao,
        sessionManager: SessionManager
    ): BlogRepository {
        return BlogRepository(
            application,
            blogxMainService,
            blogPostDao,
            sessionManager
        )
    }

    @JvmStatic
    @MainScope
    @Provides
    fun provideCreateBlogRepository(
        application: Application,
        blogxMainService: BlogxMainService,
        blogPostDao: BlogPostDao,
        sessionManager: SessionManager
    ): CreateBlogRepository {
        return CreateBlogRepository(
            application,
            blogxMainService,
            blogPostDao,
            sessionManager
        )
    }

}