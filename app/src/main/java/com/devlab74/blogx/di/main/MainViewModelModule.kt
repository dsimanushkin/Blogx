package com.devlab74.blogx.di.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.devlab74.blogx.di.main.keys.MainViewModelKey
import com.devlab74.blogx.ui.main.account.AccountViewModel
import com.devlab74.blogx.ui.main.blog.viewmodels.BlogViewModel
import com.devlab74.blogx.ui.main.create_blog.CreateBlogViewModel
import com.devlab74.blogx.viewmodels.MainViewModelFactory
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview

/**
 * This class is responsible for injecting into Main ViewModel
 */

@Module
abstract class MainViewModelModule {

    @MainScope
    @Binds
    abstract fun provideViewModelFactory(factory: MainViewModelFactory): ViewModelProvider.Factory

    @FlowPreview
    @ExperimentalCoroutinesApi
    @MainScope
    @Binds
    @IntoMap
    @MainViewModelKey(AccountViewModel::class)
    abstract fun bindAccountViewModel(accountViewModel: AccountViewModel): ViewModel

    @FlowPreview
    @ExperimentalCoroutinesApi
    @MainScope
    @Binds
    @IntoMap
    @MainViewModelKey(BlogViewModel::class)
    abstract fun bindBlogViewModel(blogViewModel: BlogViewModel): ViewModel

    @FlowPreview
    @ExperimentalCoroutinesApi
    @MainScope
    @Binds
    @IntoMap
    @MainViewModelKey(CreateBlogViewModel::class)
    abstract fun bindCreateBlogViewModel(createBlogViewModel: CreateBlogViewModel): ViewModel

}