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

/**
 * This class is responsible to provide instances for Main part of the application
 */

@FlowPreview
@Module
object MainModule {

    // Providing instance of Retrofit Builder (MainScope)
    @JvmStatic
    @MainScope
    @Provides
    fun provideBlogxMainService(retrofitBuilder: Retrofit.Builder): BlogxMainService {
        return retrofitBuilder
            .build()
            .create(BlogxMainService::class.java)
    }

    // Providing instance of Account Repository (MainScope)
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

    // Providing instance of BlogPost DAO (MainScope)
    @JvmStatic
    @MainScope
    @Provides
    fun provideBlogPostDao(db: AppDatabase): BlogPostDao {
        return db.getBlogPostDao()
    }

    // Providing instance of Blog Repository (MainScope)
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

    // Providing instance of CreateBlog Repository (MainScope)
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