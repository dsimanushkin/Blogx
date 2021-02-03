package com.devlab74.blogx.di.main

import androidx.lifecycle.ViewModel
import com.devlab74.blogx.di.ViewModelKey
import com.devlab74.blogx.ui.main.account.AccountViewModel
import dagger.Binds
import dagger.Module
import dagger.multibindings.IntoMap

@Module
abstract class MainViewModelModule {

    @Binds
    @IntoMap
    @ViewModelKey(AccountViewModel::class)
    abstract fun bindAccountViewModel(accountViewModel: AccountViewModel): ViewModel

}