package com.devlab74.blogx.di.main

import android.app.Application
import android.content.SharedPreferences
import com.devlab74.blogx.api.main.BlogxMainService
import com.devlab74.blogx.persistence.AccountPropertiesDao
import com.devlab74.blogx.persistence.AppDatabase
import com.devlab74.blogx.persistence.BlogPostDao
import com.devlab74.blogx.repository.main.*
import com.devlab74.blogx.session.SessionManager
import dagger.Module
import dagger.Provides
import kotlinx.coroutines.FlowPreview
import retrofit2.Retrofit

@FlowPreview
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
        sessionManager: SessionManager,
        editor: SharedPreferences.Editor
    ): AccountRepository {
        return AccountRepositoryImpl(
            application,
            blogxMainService,
            accountPropertiesDao,
            sessionManager,
            editor
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
        return BlogRepositoryImpl(
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
        return CreateBlogRepositoryImpl(
            application,
            blogxMainService,
            blogPostDao,
            sessionManager
        )
    }

}