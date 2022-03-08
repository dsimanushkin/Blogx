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
import com.devlab74.blogx.util.ErrorHandling.Companion.handleErrors
import com.devlab74.blogx.util.StateMessageCallback
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import timber.log.Timber
import javax.inject.Inject

/**
 * AuthActivity Class
 */

@FlowPreview
@ExperimentalCoroutinesApi
class AuthActivity : BaseActivity() {

    private lateinit var binding: ActivityAuthBinding

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
        inject()
        super.onCreate(savedInstanceState)
        binding = ActivityAuthBinding.inflate(layoutInflater)
        setContentView(binding.root)

        subscribeObservers()
        onRestoreInstanceState()
    }

    // Checking state of Navigation host and if it failed to load creating a new instance
    private fun onRestoreInstanceState() {
        val host = supportFragmentManager.findFragmentById(binding.authFragmentContainer.id)
        host?.let {
            // Do nothing here
        }?: createNavHost()
    }

    // Creating a new instance of a Navigation Host
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
        viewModel.viewState.observe(this, Observer{ viewState ->
            Timber.d("AuthActivity, subscribeObservers: AuthViewState: $viewState")
            viewState.authToken?.let{
                sessionManager.login(it)
            }
        })

        viewModel.numActiveJobs.observe(this, Observer {
            displayProgressBar(viewModel.areAnyJobsActive())
        })

        viewModel.stateMessage.observe(this, Observer { stateMessage ->
            stateMessage?.let {
                if(stateMessage.response.message == handleErrors(9008, application)){
                    onFinishCheckPreviousAuthUser()
                }

                onResponseReceived(
                    response = it.response,
                    stateMessageCallback = object: StateMessageCallback {
                        override fun removeMessageFromStack() {
                            viewModel.clearStateMessage()
                        }
                    }
                )
            }
        })

        sessionManager.cachedToken.observe(this, Observer{ token ->
            Timber.d("Auth Token: token: $token")
            token.let{ authToken ->
                if(authToken != null && authToken.accountId != "" && authToken.authToken != null){
                    navMainActivity()
                }
            }
        })
    }

    private fun checkPreviousAuthUser() {
        viewModel.setStateEvent(AuthStateEvent.CheckPreviousAuthEvent)
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

    override fun displayProgressBar(isLoading: Boolean) {
        if (isLoading) {
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.progressBar.visibility = View.INVISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        checkPreviousAuthUser()
    }

    private fun onFinishCheckPreviousAuthUser(){
        binding.fragmentContainer.visibility = View.VISIBLE
        binding.splashLogo.visibility = View.INVISIBLE
    }
}