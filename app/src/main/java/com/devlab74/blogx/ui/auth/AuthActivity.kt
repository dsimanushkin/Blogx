package com.devlab74.blogx.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.viewModels
import androidx.fragment.app.FragmentFactory
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.devlab74.blogx.BaseApplication
import com.devlab74.blogx.R
import com.devlab74.blogx.databinding.ActivityAuthBinding
import com.devlab74.blogx.fragments.auth.AuthNavHostFragment
import com.devlab74.blogx.ui.BaseActivity
import com.devlab74.blogx.ui.auth.state.AuthStateEvent
import com.devlab74.blogx.ui.main.MainActivity
import com.devlab74.blogx.viewmodels.AuthViewModelFactory
import timber.log.Timber
import javax.inject.Inject

class AuthActivity : BaseActivity() {

    private lateinit var binding: ActivityAuthBinding

    @Inject
    lateinit var factoryAuth: AuthViewModelFactory

    @Inject
    lateinit var fragmentFactory: FragmentFactory

    @Inject
    lateinit var providerFactory: ViewModelProvider.Factory

    val viewModel: AuthViewModel by viewModels {
        providerFactory
    }

    override fun inject() {
        (application as BaseApplication).authComponent().inject(this)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subscribeObservers()
        onRestoreInstanceState()
    }

    private fun onRestoreInstanceState() {
        val host = supportFragmentManager.findFragmentById(binding.authFragmentContainer.id)
        host?.let {
            // Do nothing here
        }?: createNavHost()
    }

    private fun createNavHost() {
        val navHost = AuthNavHostFragment.create(
            R.navigation.auth_nav_graph
        )
        supportFragmentManager.beginTransaction()
            .replace(binding.authFragmentContainer.id, navHost, getString(R.string.AuthNavHost))
            .setPrimaryNavigationFragment(navHost)
            .commit()
    }

    private fun subscribeObservers() {
        viewModel.dataState.observe(this, Observer { dataState ->
            onDataStateChange(dataState)
            dataState.data?.let { data ->
                data.data?.let { event ->
                    event.getContentIfNotHandled()?.let {
                        it.authToken?.let {
                            Timber.d("AuthActivity: DataState: $it")
                            viewModel.setAuthToken(it)
                        }
                    }
                }
            }
        })

        viewModel.viewState.observe(this, Observer {
            it.authToken?.let {
                sessionManager.login(it)
            }
        })

        sessionManager.cachedToken.observe(this, Observer { authToken ->
            Timber.d("AuthActivity: subscribeObservers: AuthToken: $authToken")
            if (authToken != null && authToken.accountId != "" && authToken.authToken != null) {
                navMainActivity()
            }
        })
    }

    fun checkPreviousAuthUser() {
        viewModel.setStateEvent(AuthStateEvent.CheckPreviousAuthEvent())
    }

    private fun navMainActivity() {
        val intent = Intent(this, MainActivity::class.java)
        startActivity(intent)
        finish()
        (application as BaseApplication).releaseAuthComponent()
    }

    override fun expandAppBar() {
        // Ignore it
    }

    override fun displayProgressBar(bool: Boolean) {
        if (bool) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        checkPreviousAuthUser()
    }
}