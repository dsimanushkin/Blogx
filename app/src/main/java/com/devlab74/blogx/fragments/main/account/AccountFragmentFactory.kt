package com.devlab74.blogx.fragments.main.account

import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.devlab74.blogx.di.main.MainScope
import com.devlab74.blogx.ui.main.account.AccountFragment
import com.devlab74.blogx.ui.main.account.ChangePasswordFragment
import com.devlab74.blogx.ui.main.account.UpdateAccountFragment
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import javax.inject.Inject

/**
 * Fragment factory for Main -> Account part of the app, used for instantiate fragment
 */

@MainScope
class AccountFragmentFactory
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
): FragmentFactory() {
    @ExperimentalCoroutinesApi
    @FlowPreview
    override fun instantiate(classLoader: ClassLoader, className: String) =
        when(className) {
            AccountFragment::class.java.name -> {
                AccountFragment(viewModelFactory)
            }
            UpdateAccountFragment::class.java.name -> {
                UpdateAccountFragment(viewModelFactory)
            }
            ChangePasswordFragment::class.java.name -> {
                ChangePasswordFragment(viewModelFactory)
            }
            else -> {
                AccountFragment(viewModelFactory)
            }
        }
}