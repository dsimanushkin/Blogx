package com.devlab74.blogx.di.main

import androidx.lifecycle.ViewModel
import com.devlab74.blogx.di.ViewModelKey
import com.devlab74.blogx.ui.main.account.AccountViewModel
import com.devlab74.blogx.ui.main.blog.viewmodels.BlogViewModel
import com.devlab74.blogx.ui.main.create_blog.CreateBlogViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class MainViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(AccountViewModel::class)
    abstract fun bindAccountViewModel(accountViewModel: AccountViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(BlogViewModel::class)
    abstract fun bindBlogViewModel(blogViewModel: BlogViewModel): ViewModel

    @Binds
    @IntoMap
    @ViewModelKey(CreateBlogViewModel::class)
    abstract fun bindCreateBlogViewModel(createBlogViewModel: CreateBlogViewModel): ViewModel

}