package com.devlab74.blogx.ui.auth

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.NavDestination
import com.devlab74.blogx.databinding.ActivityAuthBinding
import com.devlab74.blogx.ui.BaseActivity
import com.devlab74.blogx.ui.auth.state.AuthStateEvent
import com.devlab74.blogx.ui.main.MainActivity
import com.devlab74.blogx.viewmodels.ViewModelProviderFactory
import timber.log.Timber
import javax.inject.Inject

class AuthActivity : BaseActivity(), NavController.OnDestinationChangedListener {

    private lateinit var binding: ActivityAuthBinding

    @Inject
    lateinit var providerFactory: ViewModelProviderFactory

    lateinit var viewModel: AuthViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        viewModel = ViewModelProvider(this, providerFactory).get(AuthViewModel::class.java)

        subscribeObservers()
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
    }

    override fun expandAppBar() {
        // Ignore it
    }

    override fun onDestinationChanged(
        controller: NavController,
        destination: NavDestination,
        arguments: Bundle?
    ) {
        viewModel.cancelActiveJobs()
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