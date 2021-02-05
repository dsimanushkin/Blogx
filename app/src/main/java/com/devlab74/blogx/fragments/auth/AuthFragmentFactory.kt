package com.devlab74.blogx.fragments.auth

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.ViewModelProvider
import com.devlab74.blogx.di.auth.AuthScope
import com.devlab74.blogx.ui.auth.ForgotPasswordFragment
import com.devlab74.blogx.ui.auth.LauncherFragment
import com.devlab74.blogx.ui.auth.LoginFragment
import com.devlab74.blogx.ui.auth.RegisterFragment
import javax.inject.Inject

@AuthScope
class AuthFragmentFactory
@Inject
constructor(
    private val viewModelFactory: ViewModelProvider.Factory
): FragmentFactory() {

    override fun instantiate(classLoader: ClassLoader, className: String) =
        when(className) {
            LauncherFragment::class.java.name -> {
                LauncherFragment(viewModelFactory)
            }
            LoginFragment::class.java.name -> {
                LoginFragment(viewModelFactory)
            }
            RegisterFragment::class.java.name -> {
                RegisterFragment(viewModelFactory)
            }
            ForgotPasswordFragment::class.java.name -> {
                ForgotPasswordFragment(viewModelFactory)
            }
            else -> {
                LauncherFragment(viewModelFactory)
            }
        }


}